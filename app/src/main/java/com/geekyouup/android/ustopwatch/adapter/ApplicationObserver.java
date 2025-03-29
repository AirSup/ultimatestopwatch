package com.geekyouup.android.ustopwatch.adapter;

import static com.geekyouup.android.ustopwatch.constant.UstopwatchConsts.LOG_TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.geekyouup.android.ustopwatch.UltimateStopwatchActivity;

/**
 * 监听应用的生命周期
 */
public class ApplicationObserver implements DefaultLifecycleObserver {

    /**
     * 在应用程序的整个生命周期中只会被调用一次
     */
    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        Log.i(LOG_TAG, "application Lifecycle.Event.ON_CREATE");
    }

    /**
     * 永远不会被调用，系统不会分发调用 ON_DESTROY 事件
     */
    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        Log.i(LOG_TAG, "application Lifecycle.Event.ON_DESTROY");
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
        Log.i(LOG_TAG, "application Lifecycle.Event.ON_PAUSE");
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);
        Log.i(LOG_TAG, "application Lifecycle.Event.ON_RESUME");
    }

    /**
     * 应用处于前台
     */
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        Log.i(LOG_TAG, "application Lifecycle.Event.ON_START");
    }

    /**
     * 应用处于后台时处理
     * 当app切到后台时，主动停止，以便于重新启动时走view重建流程，使得码表view第一帧也是准确的
     * 没有这里的话，之前码表新启动时指针是准确的，但是切换后台后重新切换回来，第一帧是之前冻结的视图，指针位置不准确，刷新后才指向准确位置，
     * 是因为没有走onCreateView流程，且保留了之前冻结的视图
     * 这种特殊情况与现实情况不完全符合，故这里特殊处理一下
     * <p>
     * 问题：小米多任务里点击小窗模式页面重建时会直接使用之前的参数，导致快捷方式启动时的参数重放，不需要重建的话就不会有这个问题
     * 切换多任务切回去也会导致参数重放；
     * 解决：通过判断activity.isInMultiWindowMode()
     */
    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        Log.i(LOG_TAG, "application Lifecycle.Event.ON_STOP");
        //xx.recreate();//不能直接recreate()不然会陷入循环重建问题
        UltimateStopwatchActivity.getInstance().finish();
    }
}
