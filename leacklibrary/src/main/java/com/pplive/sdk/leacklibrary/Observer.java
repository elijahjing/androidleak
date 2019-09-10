package com.pplive.sdk.leacklibrary;

import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.pplive.sdk.leacklibrary.heap.AnalyzerServers;
import com.pplive.sdk.leacklibrary.heap.ExcludedRefs;
import com.pplive.sdk.leacklibrary.heap.HeapDump;
import com.pplive.sdk.leacklibrary.heap.Reachability;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.pplive.sdk.leacklibrary.Constants.HPROF_PATH;
import static com.pplive.sdk.leacklibrary.Retryable.Result.RETRY;
import static com.pplive.sdk.leacklibrary.Utils.checkNotNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

class Observer {
    private Set<String> retainedKeys;
    private ReferenceQueue<Object> queue;
    private GcTrigger gcTrigger;
    private ObserverExecutor observerExecutor;
    private Context context;

    Observer init(Context context) {
        this.context = context;
        queue = new ReferenceQueue<>();
        retainedKeys = new CopyOnWriteArraySet<>();
        gcTrigger = GcTrigger.DEFAULT;
        observerExecutor = new AndroidExecutor();
        return this;
    }

    void addObserver(Object observer, String referenceName) {
        checkNotNull(observer, "activity");
        checkNotNull(observer, "watchedReference");
        checkNotNull(referenceName, "referenceName");
        final long watchStartNanoTime = System.nanoTime();
        String key = UUID.randomUUID().toString();
        retainedKeys.add(key);
        final LeakWeakReference reference =
                new LeakWeakReference(observer, key, referenceName, queue);
        observerExecutor.execute(new Retryable() {
            @Override
            public Result run() {
                return checkRecycle(reference, watchStartNanoTime);
            }
        });
    }

    private Retryable.Result checkRecycle(final LeakWeakReference reference, final long watchStartNanoTime) {
        long gcStartNanoTime = System.nanoTime();
        long watchDurationMs = NANOSECONDS.toMillis(gcStartNanoTime - watchStartNanoTime);
        removeWeaklyReachableReferences();
        if (recycle(reference)) {
            return Retryable.Result.DONE;
        }
        gcTrigger.runGc();
        removeWeaklyReachableReferences();
        if (!recycle(reference)) {
            long startDumpHeap = System.nanoTime();
            long gcDurationMs = NANOSECONDS.toMillis(startDumpHeap - gcStartNanoTime);
            Toast.makeText(context,reference.name+"  有内存泄漏！开始采集...",Toast.LENGTH_LONG).show();
            //把文件保存在new  file的路径 方便后续分析取出
            String heapDumpFile=createDumpFile();
            if( null==heapDumpFile){
                return RETRY;
            }
            long heapDumpDurationMs = NANOSECONDS.toMillis(System.nanoTime() - startDumpHeap);
            HeapDump.Builder heapDumpBuilder = new HeapDump.Builder();
            HeapDump heapDump = heapDumpBuilder.heapDumpFile(new File(heapDumpFile)).referenceKey(reference.key)
                    .referenceName(reference.name)
                    .watchDurationMs(watchDurationMs)
                    .gcDurationMs(gcDurationMs)
                    .heapDumpDurationMs(heapDumpDurationMs)
                    .excludedRefs(defaultExcludedRefs())
                    .computeRetainedHeapSize(true)
                    .reachabilityInspectorClasses(defaultReachabilityInspectorClasses())
                    .build();
            //向子进程发送dump文件  让子进程处理文件

            AnalyzerServers.runAnalysis(context, heapDump);
        }
        return Retryable.Result.DONE;
    }
    private List<Class<? extends Reachability.Inspector>> defaultReachabilityInspectorClasses() {
        return Collections.emptyList();
    }
    private ExcludedRefs defaultExcludedRefs() {
        return ExcludedRefs.builder().build();
    }

    private String createDumpFile() {
        String state = android.os.Environment.getExternalStorageState();
        // 判断SdCard是否存在并且是可用的
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
            String hprofPath ;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ssss", Locale.CHINA);
            String createTime = sdf.format(new Date(System.currentTimeMillis()));
            // 判断SdCard是否存在并且是可用的
                File file = new File(Environment.getExternalStorageDirectory().getPath() + HPROF_PATH +  context.getPackageName());
                if (!file.exists()) {
                    file.mkdirs();
                }
                hprofPath =file.getPath()+ "/" + createTime + ".hprof";
                try {
                    Debug.dumpHprofData(hprofPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("保存", "保存失败");
                    return hprofPath;
                }
                Log.d("保存", "保存成功!");
                return hprofPath;
            } else {
                Log.d("保存", "no sdcard!");
                return null;
        }
    }

        private boolean recycle (LeakWeakReference reference){
            return !retainedKeys.contains(reference.key);
        }

        private void removeWeaklyReachableReferences () {
            LeakWeakReference ref;
            while ((ref = (LeakWeakReference) queue.poll()) != null) {
                retainedKeys.remove(ref.key);
            }
        }

    }
