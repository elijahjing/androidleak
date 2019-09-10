package com.pplive.sdk.leacklibrary.heap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;



import com.pplive.sdk.leacklibrary.analysis.AnalysisResultSave;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

public final class AnalyzerServers extends HeapAnalyzerProsses implements AnalyzerProgressListener {
    private static String HEAPDUMP_EXTRA = "heap_dump_data";
    public static String TAG = AnalyzerServers.class.getSimpleName();
    public AnalyzerServers() {
        super(AnalyzerServers.class.getSimpleName());
    }

    @Override
    protected void onHandleIntentInForeground(@Nullable Intent intent) {
        if (intent == null) {
            Log.e(TAG, "HeapAnalyzerService received a null intent, ignoring.");
            return;
        }
        HeapDump heapDump = (HeapDump) intent.getSerializableExtra(HEAPDUMP_EXTRA);
        HeapAnalyzer heapAnalyzer =
                new HeapAnalyzer(heapDump.excludedRefs, this, heapDump.reachabilityInspectorClasses);
        AnalysisResult result = heapAnalyzer.checkForLeak(heapDump.heapDumpFile, heapDump.referenceKey,
                heapDump.computeRetainedHeapSize);
        AnalysisResultSave.analysis(heapDump, result);
    }

    public static void runAnalysis(Context context, HeapDump file) {
        setEnabledBlocking(context, AnalyzerServers.class, true);
        Intent intent = new Intent(context, AnalyzerServers.class);
        intent.putExtra(HEAPDUMP_EXTRA, file);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void setEnabledBlocking(Context appContext, Class<?> componentClass,
                                          boolean enabled) {
        ComponentName component = new ComponentName(appContext, componentClass);
        PackageManager packageManager = appContext.getPackageManager();
        int newState = enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
        packageManager.setComponentEnabledSetting(component, newState, DONT_KILL_APP);
    }

    @Override
    public void onProgressUpdate(Step step) {
        Log.d("Analysis in progress", step.name());
    }
}
