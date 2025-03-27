package com.geekyouup.android.ustopwatch.util;

import android.text.SpannableString;

import com.geekyouup.android.ustopwatch.constant.UstopwatchConsts;

public class TimeUtils {

    private static final String START_TIME = "00:00:00.000";

    /**
     * 时间格式化成"(-)XX:XX:XX.XXX"格式
     *
     * @param time
     * @return
     */
    private static String formatTime(double time) {
        if (time == 0) return START_TIME;

        boolean isNeg = false;
        if (time < 0) {
            isNeg = true;
            time = -time;
        }

        int numHours = (int) Math.floor(time / 3600000);
        int numMins = (int) Math.floor(time / 60000 - numHours * 60);
        int numSecs = (int) Math.floor(time / 1000 - numMins * 60 - numHours * 3600);
        int numMillis = ((int) (time - numHours * 3600000 - numMins * 60000 - numSecs * 1000));

        StringBuilder mStringBuilder = new StringBuilder(20);

        if (isNeg) mStringBuilder.append('-');
        if (numHours < 10) mStringBuilder.append('0');
        mStringBuilder.append(numHours).append(':');
        if (numMins < 10) mStringBuilder.append('0');
        mStringBuilder.append(numMins).append(':');
        if (numSecs < 10) mStringBuilder.append('0');
        mStringBuilder.append(numSecs).append('.');
        if (numMillis < 100) mStringBuilder.append('0');
        if (numMillis < 10) mStringBuilder.append('0');
        mStringBuilder.append(numMillis);

        return mStringBuilder.toString();
    }

    public static SpannableString createBoldString(double time) {
        String text = formatTime(time);
        return createSpannableBold(text);
    }

    private static SpannableString createSpannableBold(String timeText) {
        SpannableString sString = null;

        if (timeText != null) {
            int textLength = timeText.length();
            //find the last inactive digit in the time string
            int nonBoldCharIndex = firstNo0Index(timeText);

            sString = new SpannableString(timeText);
            //set the active digits bold
            if (nonBoldCharIndex < textLength) {
                sString.setSpan(UstopwatchConsts.BOLD_SPAN, nonBoldCharIndex, textLength, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }

        return sString;
    }

    private static int firstNo0Index(String time) {
        int index = 0;
        while (index < time.length() && (time.charAt(index) == '0' || time.charAt(index) == ':'
                || time.charAt(index) == '.' || time.charAt(index) == '-')) {
            index++;
        }
        return index;
    }

    /**
     * style化时间字符窜
     * in the time counter, preceding 0's are grey, and active digits are black.
     * calculate the span for the text colouring
     *
     * @param time
     * @param startSpan 前面的0字符窜style
     * @param endSpan   第一个非0的后面字符窜style
     * @return
     */
    public static SpannableString toStyledString(double time, Object startSpan, Object endSpan) {
        String stime = formatTime(time);
        int spanIndex = firstNo0Index(stime);

        SpannableString sString = new SpannableString(stime);
        if (startSpan != null)
            sString.setSpan(startSpan, 0, spanIndex, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
        if (endSpan != null)
            sString.setSpan(endSpan, spanIndex, stime.length(), SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
        return sString;
    }
}
