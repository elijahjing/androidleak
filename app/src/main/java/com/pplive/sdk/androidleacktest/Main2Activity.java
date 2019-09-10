package com.pplive.sdk.androidleacktest;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main2Activity extends AppCompatActivity {
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Button  button = findViewById(R.id.test);
        //handler.sendEmptyMessageDelayed(0,10000);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             startAsyncWork();
            }
        });
    }



    @SuppressLint("StaticFieldLeak")
    void startAsyncWork() {
        Runnable work = new Runnable() {
            @Override public void run() {
                // Do some slow work in background
                SystemClock.sleep(20000);
            }
        };
        new Thread(work).start();
    }
}
