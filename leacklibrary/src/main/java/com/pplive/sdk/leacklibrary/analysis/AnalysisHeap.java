package com.pplive.sdk.leacklibrary.analysis;

import android.util.Log;

import com.pplive.sdk.leacklibrary.heap.AnalysisResult;
import com.pplive.sdk.leacklibrary.heap.HeapDump;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AnalysisHeap {
    //开始解析数据
    public static void analysis(HeapDump heapDump,AnalysisResult result){
        boolean resultSaved = false;

        boolean shouldSaveResult = result.leakFound || result.failure != null;
        if (shouldSaveResult) {
            heapDump = renameHeapdump(heapDump);
            resultSaved = saveResult(heapDump, result);
            Log.e("发现内存泄漏",result.className);
        }
    }
    private static boolean saveResult(HeapDump heapDump, AnalysisResult result) {
        File resultFile = new File(heapDump.heapDumpFile.getParentFile(),
                heapDump.heapDumpFile.getName() + ".result");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(resultFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(heapDump);
            oos.writeObject(result);
            return true;
        } catch (IOException e) {
            Log.d("--", "Could not save leak analysis result to disk.");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }


    /**
     * 保留解析的结果
     * @param heapDump
     * @return
     */
    private static HeapDump renameHeapdump(HeapDump heapDump) {
        String fileName =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS'.hprof'", Locale.US).format(new Date());

        File newFile = new File(heapDump.heapDumpFile.getParent(), fileName);
        boolean renamed = heapDump.heapDumpFile.renameTo(newFile);
        if (!renamed) {
            Log.d("文件转移", heapDump.heapDumpFile.getPath() + "====" + newFile.getPath());
        }
        return heapDump.buildUpon().heapDumpFile(newFile).build();
    }
}
