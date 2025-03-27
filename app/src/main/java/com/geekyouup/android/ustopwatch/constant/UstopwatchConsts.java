package com.geekyouup.android.ustopwatch.constant;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

public final class UstopwatchConsts {

    // Unique channel ID for notifications
    public static final String NOTIFICATION_CHANNEL_START = "ustopwatch.notifications.start";

    public static final String NOTIFICATION_CHANNEL_COUNTDOWN = "ustopwatch.notifications.countdown";

    public static final String LOG_TAG = "USW";

    /**
     * 更新数字栏时间
     */
    public static final String MSG_UPDATE_COUNTER_TIME = "msg_update_counter";

    public static final String MSG_NEW_TIME_DOUBLE = "msg_new_time_double";

    /**
     * 码表启停状态改变
     */
    public static final String MSG_STATE_CHANGE = "msg_state_change";

    public static final String MSG_REQUEST_TIME_PICKER = "msg_request_time_picker";

    public static final String MSG_COUNTDOWN_COMPLETE = "msg_countdown_complete";

    public static final String MSG_APP_RESUMING = "msg_app_resuming";

    public static final StyleSpan BOLD_SPAN = new StyleSpan(Typeface.BOLD);
}
