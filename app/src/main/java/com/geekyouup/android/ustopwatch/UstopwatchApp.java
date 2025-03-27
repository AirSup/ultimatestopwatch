package com.geekyouup.android.ustopwatch;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.geekyouup.android.ustopwatch.adapter.ApplicationObserver;

/**
 * 整个应用
 * 没有这个应用也可以运行
 * 这个需要在AndroidManifest中配置android:name
 */
public class UstopwatchApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new ApplicationObserver());
    }
}
