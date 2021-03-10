package com.example.luokaixuan.android2droid201806050939;

/**
 * @author created by luokaixuan
 * @date 2019/5/22
 * 这个类是用来干嘛的
 */

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

public class MyApplication extends Application {

    private static Context context;

    private Activity mCurrentActivity;
    private static MyApplication mApplication;

    public static MyApplication getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mApplication = this;

        registerActivityLifecycleCallbacks(new Callbacks());
    }

    public static Context getContext() {
        return context;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        mCurrentActivity = currentActivity;
    }


    class Callbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            mCurrentActivity = activity;
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}
