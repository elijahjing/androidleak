package com.pplive.sdk.leacklibrary;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class AndroidLeak {
    public static Observer observer;
    public static Application application;


    public static void init(Context context) {
        application = (Application) context.getApplicationContext();
        observer = new Observer().init(application);
        application.registerActivityLifecycleCallbacks(new LeakLifecycle() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                super.onActivityDestroyed(activity);
                observer.addObserver(activity, activity.getLocalClassName());
            }
        });
    }
}
