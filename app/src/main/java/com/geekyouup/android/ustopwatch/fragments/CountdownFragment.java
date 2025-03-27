package com.geekyouup.android.ustopwatch.fragments;

import static com.geekyouup.android.ustopwatch.constant.UstopwatchConsts.LOG_TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.geekyouup.android.ustopwatch.adapter.AlarmUpdater;
import com.geekyouup.android.ustopwatch.adapter.SoundManager;
import com.geekyouup.android.ustopwatch.constant.UstopwatchConsts;
import com.geekyouup.android.ustopwatch.util.AppUtils;
import com.geekyouup.android.ustopwatch.util.ContextUtils;
import com.geekyouup.android.ustopwatch.util.TimeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.geekyouup.android.ustopwatch.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class CountdownFragment extends Fragment {

    private StopwatchCustomVectorView mCountdownView;
    private double mCurrentTimeMillis;

    private FloatingActionButton mResetFAB;
    private TextView mTimerText;
    private SoundManager mSoundManager;

    private int mLastHour = 0;
    private int mLastMin = 0;
    private int mLastSec = 0;

    private boolean mRunningState = false;
    private boolean isReset = false;

    private static final String COUNTDOWN_PREFS = "USW_CDFRAG_PREFS";
    private static final String PREF_IS_RUNNING = "key_countdown_is_running";
    private static final String KEY_LAST_HOUR = "key_last_hour";
    private static final String KEY_LAST_MIN = "key_last_min";
    private static final String KEY_LAST_SEC = "key_last_sec";

    private int mLastSecondTicked = 0;

    //countdown picker dialog variables
    private boolean mDialogOnScreen = false;
    private static int mHoursValue = 0;
    private static int mMinsValue = 0;
    private static int mSecsValue = 0;

    private TextAppearanceSpan mTimeSpanStart;

    private TextAppearanceSpan mTimeSpanEnd;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSoundManager = SoundManager.getInstance(getContext());

        View cdView = View.inflate(getContext(), R.layout.countdown_fragment, null);
        mCountdownView = cdView.findViewById(R.id.cdview);
        mCountdownView.setHandler(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message m) {
                if (m.getData().getBoolean(UstopwatchConsts.MSG_REQUEST_TIME_PICKER, false)) {
                    requestTimeDialog();
                } else if (m.getData().getBoolean(UstopwatchConsts.MSG_COUNTDOWN_COMPLETE, false)) {
                    boolean appResuming = m.getData().getBoolean(UstopwatchConsts.MSG_APP_RESUMING, false);
                    if (!appResuming) {
                        mSoundManager.playSound(SoundManager.SOUND_COUNTDOWN_ALARM, SettingsActivity.isEndlessAlarm());
                        ContextUtils.vibrateIfCould(getContext(), 1000);
                    }

                    reset();
                } else if (m.getData().getBoolean(UstopwatchConsts.MSG_UPDATE_COUNTER_TIME, false)) {
                    mCurrentTimeMillis = m.getData().getDouble(UstopwatchConsts.MSG_NEW_TIME_DOUBLE);

                    //If we've crossed into a new second then make the tick sound
                    int currentSecond = (int) mCurrentTimeMillis / 1000;
                    if (currentSecond > mLastSecondTicked) {
                        mSoundManager.doTick();
                    }
                    mLastSecondTicked = currentSecond;
                    // 更新数字栏时间显示
                    setTextTimerTime(mCurrentTimeMillis);
                    Log.d(LOG_TAG, "update text timer complete");
                } else if (m.getData().getBoolean(UstopwatchConsts.MSG_STATE_CHANGE, false)) {
                    if (isRunning()) isReset = false;
                    setUIState();
                }
            }
        });
        mTimerText = cdView.findViewById(R.id.time_counter);
        mTimerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRunning()) requestTimeDialog();
            }
        });
        AppUtils.decorateTextTimer(mTimerText, getResources());

        //resetFAB has 2 states, if stopped it is a time picker, else it is reset
        mResetFAB = cdView.findViewById(R.id.resetfab);
        if (!isRunning() && isReset)
            mResetFAB.setImageResource(R.drawable.ic_set_countdown_time_24dp);
        else
            mResetFAB.setImageResource(R.drawable.ic_countdown_reset_24dp);
        mResetFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isReset) reset();
                else requestTimeDialog();

                mSoundManager.stopEndlessAlarm();
                mSoundManager.playSound(SoundManager.SOUND_RESET);
            }
        });

        // text timer color span
        mTimeSpanStart = new TextAppearanceSpan(getContext(), R.style.TimeTextDarkThemeDark);
        mTimeSpanEnd = new TextAppearanceSpan(getContext(), R.style.TimeTextDarkThemeLight);

        Log.i(LOG_TAG, "countdown fragment onCreateView() complete");
        return cdView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 创建视图的时候就从存储中恢复数据，避免视图可见的时候更新落后，使视图立即完整的
        this.restoreFromPreferences();
        Log.i(LOG_TAG, "countdown fragment onStart() complete");
    }

    /**
     * 可交互
     * （对用户可见）
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "countdown fragment onResume() start");
        // cancel next alarm if there is one, and clear notification bar
        AlarmUpdater.cancelCountdownAlarm(getContext());
        //
        this.restoreFromPreferences();
        Log.i(LOG_TAG, "countdown fragment onResume() complete");
    }

    /**
     * @noinspection DataFlowIssue
     */
    private void restoreFromPreferences() {
        SharedPreferences settings = getContext().getSharedPreferences(COUNTDOWN_PREFS, Context.MODE_PRIVATE);
        mLastHour = settings.getInt(KEY_LAST_HOUR, 0);
        mLastMin = settings.getInt(KEY_LAST_MIN, 0);
        mLastSec = settings.getInt(KEY_LAST_SEC, 0);
        mRunningState = settings.getBoolean(PREF_IS_RUNNING, false);
        mCountdownView.restoreState(settings);
        mCurrentTimeMillis = mCountdownView.getWatchTime();
        Log.d(LOG_TAG, "countdown fragment restoreFromPreferences() complete");
    }

    public void reset() {
        mCountdownView.setTime(mLastHour, mLastMin, mLastSec, true);
        isReset = true;
        setUIState();
    }

    /**
     * 设置数字栏时间显示
     *
     * @param millis
     */
    private void setTextTimerTime(double millis) {
        mTimerText.setText(TimeUtils.toStyledString(millis, mTimeSpanStart, mTimeSpanEnd));
    }

    public boolean isRunning() {
        return (mCountdownView != null && mCountdownView.isRunning());
    }

    private void setUIState() {
        boolean stateChanged = (mRunningState != isRunning());
        mRunningState = isRunning();

        if (!isRunning() && isReset) {
            mResetFAB.setImageResource(R.drawable.ic_set_countdown_time_24dp);
        } else {
            mResetFAB.setImageResource(R.drawable.ic_countdown_reset_24dp);
            if (stateChanged)
                mSoundManager.playSound(isRunning() ? SoundManager.SOUND_START : SoundManager.SOUND_STOP);
        }
    }

    /**
     * @noinspection DataFlowIssue
     */
    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences settings = getContext().getSharedPreferences(COUNTDOWN_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_IS_RUNNING, mRunningState);
        mCountdownView.saveState(editor);
        editor.putInt(KEY_LAST_HOUR, mLastHour);
        editor.putInt(KEY_LAST_MIN, mLastMin);
        editor.putInt(KEY_LAST_SEC, mLastSec);
        editor.apply();
        Log.i(LOG_TAG, "countdown fragment onPause() complete");
    }

    @Override
    public void onStop() {
        super.onStop();
        //
        mCountdownView.stop();
        Log.i(LOG_TAG, "countdown fragment onStop() complete");
    }

    private void requestTimeDialog() {
        // stop stacking of dialogs
        if (mDialogOnScreen)
            return;

        if (mHoursValue == 0) mHoursValue = mLastHour;
        if (mMinsValue == 0) mMinsValue = mLastMin;
        if (mSecsValue == 0) mSecsValue = mLastSec;

        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getContext(), R.style.AppTheme_PickerDialog);
        //ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getContext(), androidx.appcompat.R.style.Theme_AppCompat);
        View countdownPickerView = LayoutInflater.from(contextWrapper).inflate(R.layout.countdown_picker, null);
        NumberPicker npHours = countdownPickerView.findViewById(R.id.numberPickerHours);
        npHours.setMaxValue(99);
        npHours.setValue(mHoursValue);
        NumberPicker npMins = countdownPickerView.findViewById(R.id.numberPickerMins);
        npMins.setMaxValue(59);
        npMins.setValue(mMinsValue);
        NumberPicker npSecs = countdownPickerView.findViewById(R.id.numberPickerSecs);
        npSecs.setMaxValue(59);
        npSecs.setValue(mSecsValue);
        //
        AlertDialog mSelectTime = new AlertDialog.Builder(contextWrapper).create();
        mSelectTime.setView(countdownPickerView);
        mSelectTime.setTitle(getString(R.string.timer_title));
        mSelectTime.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.timer_start),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDialogOnScreen = false;
                        mHoursValue = npHours.getValue();
                        mMinsValue = npMins.getValue();
                        mSecsValue = npSecs.getValue();
                        setTime(mHoursValue, mMinsValue, mSecsValue);
                    }
                });
        mSelectTime.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.timer_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDialogOnScreen = false;
                    }
                });
        mSelectTime.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mDialogOnScreen = false;
            }
        });
        mSelectTime.show();

        mDialogOnScreen = true;
    }

    public void setTime(int hour, int minute, int seconds) {
        mLastHour = hour;
        mLastMin = minute;
        mLastSec = seconds;
        mCountdownView.setTime(mLastHour, mLastMin, mLastSec, false);
        setUIState();
    }
}
