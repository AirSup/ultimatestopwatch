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

    public static final String PAGE_STOPWATCH = "stopwatch";

    public static final String PAGE_COUNTDOWN = "countdown";

    public static final String PAGE_KEY = "current_page";

    public static final String PAGE_KEY_ACTION = "page_action";

    public static final String PAGE_ACTION_START = "start";

    public static final String PAGE_ACTION_RESTART = "restart";

    public static final int NID_STOPWATCH_START = 100;

    public static final int NID_COUNTDOWN_START = 101;

    public static final int NID_COUNTDOWN_DONE = 102;

    public static final String INTENT_EXTRA_LAUNCH_COUNTDOWN = "launch_countdown";
}
