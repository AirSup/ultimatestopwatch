package com.geekyouup.android.ustopwatch.util;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.geekyouup.android.ustopwatch.SettingsActivity;

/**
 * @noinspection unused
 */
public final class ContextUtils {

    /**
     * 获取当前apk的版本号
     *
     * @param mContext
     * @return
     */
    public static int getVersionCode(Context mContext) {
        int code = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            code = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            Log.e("USW", "Get version code Error", e);
        }
        return code;
    }

    /**
     * 获取当前apk的版本名
     *
     * @param context 上下文
     * @return
     */
    public static String getVersionName(Context context) {
        String name = "";
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionName
            name = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            Log.e("USW", "Get version name Error", e);
        }
        return name;
    }

    public static boolean isSysNightMode(Context context) {
        // 判断系统应用了何种模式
        boolean nightMode = false;
        try {
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
            nightMode = uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
        } catch (Exception e) {
            Log.e("USW", "Get ui mode manager Error", e);
        }
        return nightMode;
    }

    public static boolean isAppNightMode(Context context) {
        // 判断此app应用了何种模式
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Vibrate constantly for the specified period of time.
     *
     * @param context
     * @param milliseconds The number of milliseconds to vibrate.
     */
    public static void vibrateIfCould(Context context, long milliseconds) {
        try {
            if (SettingsActivity.isVibrate()) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// api 26
                    vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {// deprecated api
                    vibrator.vibrate(milliseconds);
                }
            }
        } catch (Exception e) {
            Log.e("USW", "vibrate If Could Error", e);
        }
    }
}
