package com.pplive.sdk.leacklibrary;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.pplive.sdk.leacklibrary.heap.AnalysisResult;
import com.pplive.sdk.leacklibrary.heap.HeapDump;

import static android.text.format.Formatter.formatShortFileSize;

public class AndroidLeak {
    public static Application application;
    public static void init(Context context) {
         application = (Application) context.getApplicationContext();
        final Observer  observer = new Observer().init(application);
        application.registerActivityLifecycleCallbacks(new LeakLifecycle() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                super.onActivityDestroyed(activity);
                observer.addObserver(activity, activity.getLocalClassName());
            }
        });
    }
    /** Returns a string representation of the result of a heap analysis. */
    public static String leakInfo(Context context, HeapDump heapDump, AnalysisResult result,
                                  boolean detailed) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        String versionName = packageInfo.versionName;
        int versionCode = packageInfo.versionCode;
        String info = "In " + packageName + ":" + versionName + ":" + versionCode + ".\n";
        String detailedString = "";
        if (result.leakFound) {
            if (result.excludedLeak) {
                info += "* EXCLUDED LEAK.\n";
            }
            info += "* " + result.className;
            if (!heapDump.referenceName.equals("")) {
                info += " (" + heapDump.referenceName + ")";
            }
            info += " has leaked:\n" + result.leakTrace.toString() + "\n";
            if (result.retainedHeapSize != AnalysisResult.RETAINED_HEAP_SKIPPED) {
                info += "* Retaining: " + formatShortFileSize(context, result.retainedHeapSize) + ".\n";
            }
            if (detailed) {
                detailedString = "\n* Details:\n" + result.leakTrace.toDetailedString();
            }
        } else if (result.failure != null) {
            // We duplicate the library version & Sha information because bug reports often only contain
            // the stacktrace.
            info += "* FAILURE in " + 2 + " " + 2 + ":" + Log.getStackTraceString(
                    result.failure) + "\n";
        } else {
            info += "* NO LEAK FOUND.\n\n";
        }
        if (detailed) {
            detailedString += "* Excluded Refs:\n" + heapDump.excludedRefs;
        }

        info += "* Reference Key: "
                + heapDump.referenceKey
                + "\n"
                + "* Device: "
                + Build.MANUFACTURER
                + " "
                + Build.BRAND
                + " "
                + Build.MODEL
                + " "
                + Build.PRODUCT
                + "\n"
                + "* Android Version: "
                + Build.VERSION.RELEASE
                + " API: "
                + Build.VERSION.SDK_INT
                + " LeakCanary: "
                + "\n"
                + "* Durations: watch="
                + heapDump.watchDurationMs
                + "ms, gc="
                + heapDump.gcDurationMs
                + "ms, heap dump="
                + heapDump.heapDumpDurationMs
                + "ms, analysis="
                + result.analysisDurationMs
                + "ms"
                + "\n"
                + detailedString;

        return info;
    }
}
