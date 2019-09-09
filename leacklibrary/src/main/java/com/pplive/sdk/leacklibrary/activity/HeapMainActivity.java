package com.pplive.sdk.leacklibrary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.pplive.sdk.leacklibrary.R;
import com.pplive.sdk.leacklibrary.heap.AnalysisResult;

import static com.pplive.sdk.leacklibrary.activity.HeapActivity.SHOW_DETAIL_EXTRA;
import static com.pplive.sdk.leacklibrary.activity.HeapActivity.result;

public class HeapMainActivity extends AppCompatActivity {

    public AnalysisResult analysisResult;
    public String referenceName="";
    MainAdapter myAdapter;
    ListView listView;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heap_activity_main);
        View view=findViewById(R.id.root);
        Intent intent = getIntent();
        if (intent.hasExtra(SHOW_DETAIL_EXTRA)) {
            referenceName = intent.getStringExtra(SHOW_DETAIL_EXTRA);
        }
        listView = findViewById(R.id.listview_main);
        setViewTreeObserver(view);
        analysisResult= result;
        updateUi();
    }


    public static void setViewTreeObserver(View v) {
        v.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            View view = v.findFocus();
            if (view != null) {
                Log.e("焦点已经改变", "" + view);
            } else {
                Log.e("焦点已经改变", "null");

            }

        });

    }
    public void updateUi() {
        Log.e("sddddsdsdd", "" + new Gson().toJson(analysisResult).toString());

        if (analysisResult == null||analysisResult.leakTrace==null) {
            return;
        }

        if (myAdapter == null) {
            myAdapter = new MainAdapter(analysisResult.leakTrace, this,referenceName);
            listView.setAdapter(myAdapter);
        } else {
            myAdapter.setData(analysisResult.leakTrace,referenceName);
        }
        Log.e("sddddsdsdd", "" + new Gson().toJson(analysisResult).toString());
    }

}
