package com.geekyouup.android.ustopwatch.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.widget.TextView;

public final class AppUtils {

    /**
     * 将mTimerText数字时间栏置中、左对齐，避免字体宽度变化导致字体来回跳动(有残影)
     *
     * @param timerText
     * @param resources
     */
    public static void decorateTextTimer(TextView timerText, Resources resources) {
        Rect bounds = new Rect();
        String viewText = timerText.getText().toString();
        timerText.getPaint().getTextBounds(viewText, 0, viewText.length(), bounds);
        int textWidth = bounds.width();
        int width = resources.getDisplayMetrics().widthPixels;
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            width = width / 2;
        timerText.setPadding((width - textWidth) / 2, 0, 0, 0);
    }
}
