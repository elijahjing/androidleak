package com.pplive.sdk.leacklibrary;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class AndroidLeak {
    private static Observer observer ;

    public static void init(Context context) {
        Application application = (Application) context.getApplicationContext();
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
