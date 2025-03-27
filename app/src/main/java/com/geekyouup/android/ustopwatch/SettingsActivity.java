package com.geekyouup.android.ustopwatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.geekyouup.android.ustopwatch.util.ContextUtils;

public class SettingsActivity extends AppCompatActivity {

    private static boolean isTicking = false;
    private static boolean isLaptimerEnabled = false;
    private static boolean isEndlessAlarm = false;
    private static boolean isVibrate = true;
    private static boolean isAnimating = true;
    private static int themeSelect = 2;
    private static boolean isKeepScreenOn = true;
    private static int orientationSelect = 2;
    private static int languageSelect = 0;
    private static final String KEY_TICKING = "key_ticking_on";
    private static final String KEY_ENDLESS_ALARM = "key_endless_alarm_on";
    private static final String KEY_VIBRATE = "key_vibrate_on";
    private static final String KEY_ANIMATING = "key_animations_on";
    private static final String KEY_LAP_TIMER = "key_laptimer_on";
    private static final String KEY_THEME_SELECT = "key_theme_select";
    private static final String KEY_KEEP_SCREEN_ON = "key_keep_screen_on";
    private static final String KEY_ORIENTATION = "key_orientation";
    private static final String KEY_LANGUAGE = "key_language";

    private static final int THEME_POSITION_DAY = 0;
    private static final int THEME_POSITION_NIGHT = 1;
    private static final int THEME_POSITION_SYSTEM = 2;

    private static final int ORIENTATION_POSITION_PORTRAIT = 0;
    private static final int ORIENTATION_POSITION_LANDSCAPE = 1;
    private static final int ORIENTATION_POSITION_SYSTEM = 2;

    private static final int LANGUAGE_POSITION_SYS = 0;
    private static final int LANGUAGE_POSITION_EN = 1;
    private static final int LANGUAGE_POSITION_CH = 2;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // 语言
        Spinner mLanguageSpinner = findViewById(R.id.settings_language);
        String[] langArray = getResources().getStringArray(R.array.arr_language);
        mLanguageSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, langArray));
        mLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                languageSelect = position;
                // position start from 0
                if (LANGUAGE_POSITION_EN == position) { // 英文
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
                } else if (LANGUAGE_POSITION_CH == position) { // 中文
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zh"));
                } else { // 跟随系统
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat. getEmptyLocaleList());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 主题模式
        Spinner mThemeSpinner = findViewById(R.id.settings_theme_mode);
        String[] themeArray = getResources().getStringArray(R.array.arr_theme_mode);
        mThemeSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, themeArray));
        mThemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                themeSelect = position;
                // position start from 0
                if (THEME_POSITION_DAY == position) { // 日间模式
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (THEME_POSITION_NIGHT == position) { // 夜间模式
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else { // 跟随系统
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 屏幕方向
        Spinner mOrientationSpinner = findViewById(R.id.settings_orientation);
        String[] orientationArray = getResources().getStringArray(R.array.arr_orientation);
        mOrientationSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, orientationArray));
        mOrientationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * onCreate时会根据之前的值自动调用一次这里，不需要用户操作
             *
             * @param parent The AdapterView where the selection happened
             * @param view The view within the AdapterView that was clicked
             * @param position The position of the view in the adapter
             * @param id The row id of the item that is selected
             */
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                orientationSelect = position;
                // position start from 0
                if (ORIENTATION_POSITION_PORTRAIT == position) { // 竖屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                } else if (ORIENTATION_POSITION_LANDSCAPE == position) { // 横屏
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else { // 跟随系统; FULL_SENSOR会忽略系统“锁定方向”设定;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        CompoundButton mSwitchLaptimer = findViewById(R.id.settings_seconds_laptimer);
        mSwitchLaptimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isLaptimerEnabled = b;
            }
        });

        CompoundButton mSwitchSoundTicking = findViewById(R.id.settings_seconds_sound);
        mSwitchSoundTicking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isTicking = b;
            }
        });

        CompoundButton mSwitchAnimating = findViewById(R.id.settings_animations);
        mSwitchAnimating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isAnimating = b;
            }
        });

        CompoundButton mSwitchEndlessAlarm = findViewById(R.id.settings_endless_alert);
        mSwitchEndlessAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isEndlessAlarm = b;
            }
        });

        CompoundButton mSwitchVibrate = findViewById(R.id.settings_vibrate);
        mSwitchVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isVibrate = b;
            }
        });

        CompoundButton mSwitchKeepScreenOn = findViewById(R.id.settings_keep_screen_on);
        mSwitchKeepScreenOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isKeepScreenOn = b;
            }
        });

        mThemeSpinner.setSelection(themeSelect);
        mSwitchLaptimer.setChecked(isLaptimerEnabled);
        mSwitchEndlessAlarm.setChecked(isEndlessAlarm);
        mSwitchSoundTicking.setChecked(isTicking);
        mSwitchVibrate.setChecked(isVibrate);
        mSwitchKeepScreenOn.setChecked(isKeepScreenOn);
        mOrientationSpinner.setSelection(orientationSelect);
        mSwitchAnimating.setChecked(isAnimating);
        mLanguageSpinner.setSelection(languageSelect);

        if (!((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).hasVibrator()) {
            mSwitchVibrate.setChecked(false);
            mSwitchVibrate.setEnabled(false);
        }

        // get version name from package info and set it to text view
        TextView textView = findViewById(R.id.settings_version);
        String versionName = textView.getText() + ContextUtils.getVersionName(this);
        textView.setText(versionName);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences settings = getSharedPreferences(UltimateStopwatchActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_TICKING, isTicking);
        editor.putBoolean(KEY_ENDLESS_ALARM, isEndlessAlarm);
        editor.putBoolean(KEY_VIBRATE, isVibrate);
        editor.putBoolean(KEY_ANIMATING, isAnimating);
        editor.putBoolean(KEY_LAP_TIMER, isLaptimerEnabled);
        editor.putInt(KEY_THEME_SELECT, themeSelect);
        editor.putBoolean(KEY_KEEP_SCREEN_ON, isKeepScreenOn);
        editor.putInt(KEY_ORIENTATION, orientationSelect);
        editor.putInt(KEY_LANGUAGE, languageSelect);
        editor.apply();
    }

    /**
     * Called from parent Activity to ensure all settings are always loaded
     * 这里的defValue为默认值
     *
     * @param prefs
     */
    public static void loadSettings(SharedPreferences prefs) {
        isLaptimerEnabled = prefs.getBoolean(KEY_LAP_TIMER, false);
        isTicking = prefs.getBoolean(KEY_TICKING, false);
        isEndlessAlarm = prefs.getBoolean(KEY_ENDLESS_ALARM, false);
        isVibrate = prefs.getBoolean(KEY_VIBRATE, true);
        isAnimating = prefs.getBoolean(KEY_ANIMATING, true);
        themeSelect = prefs.getInt(KEY_THEME_SELECT, THEME_POSITION_SYSTEM);
        isKeepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true);
        orientationSelect = prefs.getInt(KEY_ORIENTATION, ORIENTATION_POSITION_SYSTEM);
        languageSelect = prefs.getInt(KEY_LANGUAGE, LANGUAGE_POSITION_SYS);
    }

    public static boolean isTicking() {
        return isTicking;
    }

    public static boolean isEndlessAlarm() {
        return isEndlessAlarm;
    }

    public static boolean isVibrate() {
        return isVibrate;
    }

    public static boolean isAnimating() {
        return isAnimating;
    }

    public static boolean isLaptimerEnabled() {
        return isLaptimerEnabled;
    }

    public static boolean isUseNightMode(Context context) {
        boolean useNightMode;
        switch (themeSelect) {
            case THEME_POSITION_DAY:
                useNightMode = false;
                break;
            case THEME_POSITION_NIGHT:
                useNightMode = true;
                break;
            case THEME_POSITION_SYSTEM:
                // same with default
            default:
                // 默认、跟随系统：判断系统当前主题模式
                useNightMode = ContextUtils.isSysNightMode(context);
        }
        return useNightMode;
    }

    public static boolean isKeepScreenOn() {
        return isKeepScreenOn;
    }

    public static int getOrientation() {
        int rtn;
        if (orientationSelect == ORIENTATION_POSITION_PORTRAIT) {
            rtn = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        } else if (orientationSelect == ORIENTATION_POSITION_LANDSCAPE) {
            rtn = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        } else {
            rtn = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
        return rtn;
    }
}
