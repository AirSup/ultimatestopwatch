package com.geekyouup.android.ustopwatch.fragments;

import static com.geekyouup.android.ustopwatch.constant.UstopwatchConsts.LOG_TAG;

import android.os.Looper;
import android.os.Message;

import com.geekyouup.android.ustopwatch.adapter.AlarmUpdater;
import com.geekyouup.android.ustopwatch.adapter.SoundManager;
import com.geekyouup.android.ustopwatch.constant.UstopwatchConsts;
import com.geekyouup.android.ustopwatch.util.AppUtils;
import com.geekyouup.android.ustopwatch.util.TimeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.widget.TextView;

import com.geekyouup.android.ustopwatch.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class StopwatchFragment extends Fragment {

    private StopwatchCustomVectorView mStopwatchView;
    private TextView mTimerText;
    private double mCurrentTimeMillis = 0;
    private SoundManager mSoundManager;
    private boolean mRunningState = false;
    // 默认数据已经保存，该次需从配置读取数据
    private boolean isStateSaved = true;

    private static final String PREFS_NAME = "USW_SWFRAG_PREFS";
    private static final String PREF_IS_RUNNING = "key_stopwatch_is_running";

    private int mLastSecond = 0;
    private FloatingActionButton mResetFAB;
    private FloatingActionButton mLaptimeFAB;

    private TextAppearanceSpan timeStartSpan;
    private TextAppearanceSpan timeEndSpan;

    /**
     * on create view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSoundManager = SoundManager.getInstance(getActivity());

        View swView = View.inflate(getContext(), R.layout.stopwatch_fragment, null);
        mTimerText = swView.findViewById(R.id.counter_text);
        AppUtils.decorateTextTimer(mTimerText, getResources());

        mStopwatchView = swView.findViewById(R.id.swview);
        mStopwatchView.setHandler(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message m) {
                // 时间连续变动（毫秒级别），秒针tick只有每秒响
                if (m.getData().getBoolean(UstopwatchConsts.MSG_UPDATE_COUNTER_TIME, false)) {
                    // 底部圆盘更新消息到数字栏更新数字时间显示
                    mCurrentTimeMillis = m.getData().getDouble(UstopwatchConsts.MSG_NEW_TIME_DOUBLE);
                    setTextTimerTime(mCurrentTimeMillis);

                    int currentSecond = (int) mCurrentTimeMillis / 1000;
                    if (currentSecond > mLastSecond) {
                        mSoundManager.doTick();
                    }
                    mLastSecond = currentSecond;
                }
                // 设变量、发声
                else if (m.getData().getBoolean(UstopwatchConsts.MSG_STATE_CHANGE, false)) {
                    setUIState();
                }
            }
        });

        // 重置按钮
        mResetFAB = swView.findViewById(R.id.resetfab);
        mResetFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
        // 记圈按钮; show/hide the Lap Time button depending on state
        mLaptimeFAB = swView.findViewById(R.id.laptimefab);
        if (!SettingsActivity.isLaptimerEnabled()) mLaptimeFAB.hide();
        else {
            mLaptimeFAB.show();
            mLaptimeFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRunning()) {
                        LapTimeRecorder.getInstance().recordLapTime(mStopwatchView.getWatchTime(), (UltimateStopwatchActivity) getActivity());
                        mSoundManager.playSound(SoundManager.SOUND_LAPTIME);
                    }
                }
            });
        }
        // text timer color span
        timeStartSpan = new TextAppearanceSpan(getContext(), R.style.TimeTextStart);
        timeEndSpan = new TextAppearanceSpan(getContext(), R.style.TimeTextEnd);

        Log.i(LOG_TAG, "stopwatch fragment onCreateView() complete");
        return swView;
    }

    private void reset() {
        LapTimeRecorder.getInstance().stopwatchReset();
        mStopwatchView.setTime(0, 0, 0, true);
        mSoundManager.playSound(SoundManager.SOUND_RESET);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 在onStart也调用为避免在onResume开启更新线程慢了导致指针跳动
        this.restoreFromPreferences();
        Bundle activityBundle = getArguments();
        String action;
        if (activityBundle != null
                && UstopwatchConsts.PAGE_STOPWATCH.equalsIgnoreCase(activityBundle.getString(UstopwatchConsts.PAGE_KEY))) {
            action = activityBundle.getString(UstopwatchConsts.PAGE_KEY_ACTION);
            // 快捷方式动作
            if (UstopwatchConsts.PAGE_ACTION_START.equalsIgnoreCase(action)) {
                if (!mRunningState) {
                    mStopwatchView.startStop();
                }
            } else if (UstopwatchConsts.PAGE_ACTION_RESTART.equalsIgnoreCase(action)) {
                this.reset();
                mStopwatchView.startStop();
            }
            //
            activityBundle.clear();
        }
        //
        Log.i(LOG_TAG, "stopwatch fragment onStart() complete");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "stopwatch fragment onResume() start");
        AlarmUpdater.cancelStopwatchNotification(getActivity());
        this.restoreFromPreferences();
        Log.i(LOG_TAG, "stopwatch fragment onResume() complete");
    }

    /**
     * 从存储中恢复数据
     *
     * @noinspection DataFlowIssue
     */
    private void restoreFromPreferences() {
        if (!this.isStateSaved) {
            return;
        }
        //
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mRunningState = settings.getBoolean(PREF_IS_RUNNING, false);
        mStopwatchView.restoreState(settings);
        this.isStateSaved = false;
    }

    /**
     * 钟表起停状态改变时，改变量、发声
     */
    private void setUIState() {
        boolean stateChanged = (mRunningState != isRunning());
        mRunningState = isRunning();

        if (stateChanged)
            mSoundManager.playSound(mRunningState ? SoundManager.SOUND_START : SoundManager.SOUND_STOP);
    }

    /**
     * 设置数字时间显示栏的时间文本（会已一定格式显示）
     * （这里的文本在XiaoMi14上，字体sans-serif-light会导致:在变粗前会下垂，是小米系统问题，不是代码问题）
     *
     * @param millis
     */
    private void setTextTimerTime(double millis) {
        mTimerText.setText(TimeUtils.toStyledString(millis, timeStartSpan, timeEndSpan));
    }

    public boolean isRunning() {
        return (mStopwatchView != null && mStopwatchView.isRunning());
    }

    /**
     * @noinspection DataFlowIssue
     */
    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_IS_RUNNING, mRunningState);
        mStopwatchView.saveState(editor);
        editor.apply();
        this.isStateSaved = true;
        Log.i(LOG_TAG, "stopwatch fragment onPause() complete");
    }

    @Override
    public void onStop() {
        super.onStop();
        // 从ViewPager切换到ViewPager2后其他fragment切换到当前码表fragment时指针大跳的情况
        // 由于FragmentStateAdapter比FragmentPagerAdapter调用onResume是晚一个fragment的，FragmentPagerAdapter会预先调用下一个fragment
        // 的onResume，而FragmentStateAdapter不会，导致FragmentStateAdapter修改的TabsFragmentAdapter会是指针位置滞后，从而出现
        // 指针大跳的情况，所以将mStopwatchView.stop()从onPause()迁移到onStop()使后台依然计算
        // 切换到后台后依旧在不停onDraw()，理论上浪费资源，但是保证了实时性且符合现实模拟
        // 从onPause()移动到这里避免指针大跳
        mStopwatchView.stop();
        Log.i(LOG_TAG, "stopwatch fragment onStop() complete");
    }

    public void notifyIfNecessary() {
        try {
            if (mRunningState && mCurrentTimeMillis > 0) {
                // show notification
                AlarmUpdater.notifyStopwatch(getActivity(), (long) mCurrentTimeMillis);
            }
        } catch (Exception ignored) {
        }
    }
}
