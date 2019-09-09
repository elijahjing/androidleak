package com.pplive.sdk.androidleacktest;

import android.annotation.SuppressLint;
import android.app.Application;

import com.pplive.sdk.leacklibrary.AndroidLeak;
import com.squareup.leakcanary.LeakCanary;

@SuppressLint("Registered")
public class ExampleApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        AndroidLeak.init(this);
    }

}
