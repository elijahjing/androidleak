/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pplive.sdk.leacklibrary.analysis;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.pplive.sdk.leacklibrary.analysis.HeapDumper.RETRY_LATER;

public final class DefaultLeakDirectoryProvider implements LeakDirectoryProvider {

  private static final int DEFAULT_MAX_STORED_HEAP_DUMPS = 7;

  private static final String HPROF_SUFFIX = ".hprof";
  private static final String PENDING_HEAPDUMP_SUFFIX = "_pending" + HPROF_SUFFIX;

  /** 10 minutes */
  private static final int ANALYSIS_MAX_DURATION_MS = 10 * 60 * 1000;

  private final Context context;
  private final int maxStoredHeapDumps;

  private volatile boolean writeExternalStorageGranted;
  private volatile boolean permissionNotificationDisplayed;

  public DefaultLeakDirectoryProvider(Context context) {
    this(context, DEFAULT_MAX_STORED_HEAP_DUMPS);
  }

  public DefaultLeakDirectoryProvider(Context context, int maxStoredHeapDumps) {
    if (maxStoredHeapDumps < 1) {
      throw new IllegalArgumentException("maxStoredHeapDumps must be at least 1");
    }
    this.context = context.getApplicationContext();
    this.maxStoredHeapDumps = maxStoredHeapDumps;
  }

  @Override
  public List<File> listFiles(FilenameFilter filter) {
    if (!hasStoragePermission()) {
      Log.e("hasStoragePermission"," 没有权限");
    }
    List<File> files = new ArrayList<>();

    File[] externalFiles = externalStorageDirectory().listFiles(filter);
    if (externalFiles != null) {
      files.addAll(Arrays.asList(externalFiles));
    }

    File[] appFiles = appStorageDirectory().listFiles(filter);
    if (appFiles != null) {
      files.addAll(Arrays.asList(appFiles));
    }
    return files;
  }

  @Override
  public File newHeapDumpFile() {
    List<File> pendingHeapDumps = listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String filename) {
        return filename.endsWith(PENDING_HEAPDUMP_SUFFIX);
      }
    });

    // If a new heap dump file has been created recently and hasn't been processed yet, we skip.
    // Otherwise we move forward and assume that the analyzer process crashes. The file will
    // eventually be removed with heap dump file rotation.
    for (File file : pendingHeapDumps) {
      if (System.currentTimeMillis() - file.lastModified() < ANALYSIS_MAX_DURATION_MS) {
        return RETRY_LATER;
      }
    }

    cleanupOldHeapDumps();

    File storageDirectory = externalStorageDirectory();
    if (!directoryWritableAfterMkdirs(storageDirectory)) {
      if (!hasStoragePermission()) {
      } else {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
        } else {
        }
      }
      // Fallback to app storage.
      storageDirectory = appStorageDirectory();
      if (!directoryWritableAfterMkdirs(storageDirectory)) {

        return RETRY_LATER;
      }
    }
    // If two processes from the same app get to this step at the same time, they could both
    // create a heap dump. This is an edge case we ignore.
    return new File(storageDirectory, UUID.randomUUID().toString() + PENDING_HEAPDUMP_SUFFIX);
  }

  @Override
  public void clearLeakDirectory() {
    List<File> allFilesExceptPending = listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String filename) {
        return !filename.endsWith(PENDING_HEAPDUMP_SUFFIX);
      }
    });
    for (File file : allFilesExceptPending) {
      boolean deleted = file.delete();
      if (!deleted) {
      }
    }
  }

  @TargetApi(M) private boolean hasStoragePermission() {
    if (SDK_INT < M) {
      return true;
    }
    // Once true, this won't change for the life of the process so we can cache it.
    if (writeExternalStorageGranted) {
      return true;
    }
    writeExternalStorageGranted =
        context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    return writeExternalStorageGranted;
  }


  private File externalStorageDirectory() {
    File downloadsDirectory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
    return new File(downloadsDirectory, "leakcanary-" + context.getPackageName());
  }

  private File appStorageDirectory() {
    File appFilesDirectory = context.getFilesDir();
    return new File(appFilesDirectory, "leakcanary");
  }

  private boolean directoryWritableAfterMkdirs(File directory) {
    boolean success = directory.mkdirs();
    return (success || directory.exists()) && directory.canWrite();
  }

  private void cleanupOldHeapDumps() {
    List<File> hprofFiles = listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String filename) {
        return filename.endsWith(HPROF_SUFFIX);
      }
    });
    int filesToRemove = hprofFiles.size() - maxStoredHeapDumps;
    if (filesToRemove > 0) {
      // Sort with oldest modified first.
      Collections.sort(hprofFiles, new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
          return Long.valueOf(lhs.lastModified()).compareTo(rhs.lastModified());
        }
      });
      for (int i = 0; i < filesToRemove; i++) {
        boolean deleted = hprofFiles.get(i).delete();
        if (!deleted) {
        }
      }
    }
  }
}
