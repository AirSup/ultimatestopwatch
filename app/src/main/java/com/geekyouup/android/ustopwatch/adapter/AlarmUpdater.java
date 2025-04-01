package com.geekyouup.android.ustopwatch.adapter;


import static com.geekyouup.android.ustopwatch.constant.UstopwatchConsts.INTENT_EXTRA_LAUNCH_COUNTDOWN;
import static com.geekyouup.android.ustopwatch.constant.UstopwatchConsts.NID_COUNTDOWN_DONE;
import static com.geekyouup.android.ustopwatch.constant.UstopwatchConsts.NID_COUNTDOWN_START;
import static com.geekyouup.android.ustopwatch.constant.UstopwatchConsts.NID_STOPWATCH_START;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.geekyouup.android.ustopwatch.R;
import com.geekyouup.android.ustopwatch.UltimateStopwatchActivity;
import com.geekyouup.android.ustopwatch.constant.UstopwatchConsts;

public class AlarmUpdater {

    public static void cancelCountdownAlarm(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent defineIntent = new Intent(context, UpdateService.class);
            defineIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent piWakeUp = PendingIntent.getService(context, 0, defineIntent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            //
            if (piWakeUp != null) alarmManager.cancel(piWakeUp);
            cancelNotification(context, NID_COUNTDOWN_DONE);
        } catch (Exception ignored) {
        }
    }

    /**
     * cancels alarm then sets new one
     * 时间触发不一定准确
     *
     * @param context
     * @param inMillis
     */
    public static void setCountdownAlarm(Context context, long inMillis) {
        if (inMillis < 0) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent defineIntent = new Intent(context, UpdateService.class);
        defineIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent piWakeUp = PendingIntent.getService(context, 0, defineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // 定时可能会延后触发，所以不一定确保准确
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + inMillis, piWakeUp);
    }

    public static class UpdateService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            notifyStatusBar();
            stopSelf();
            return START_NOT_STICKY;
        }

        //show Countdown Complete notification
        private void notifyStatusBar() {
            cancelCountdownNotification(this);

            // The PendingIntent to launch our activity if the user selects this notification
            Intent launcher = new Intent(this, UltimateStopwatchActivity.class);
            launcher.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            launcher.putExtra(INTENT_EXTRA_LAUNCH_COUNTDOWN, true);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launcher,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            // Set the icon, scrolling text and timestamp
            Notification notification = new NotificationCompat.Builder(this, UstopwatchConsts.NOTIFICATION_CHANNEL_COUNTDOWN)
                    .setContentTitle(getString(R.string.app_name)) // Title displayed in the notification
                    .setContentText(getString(R.string.countdown_complete)) // Text displayed in the notification
                    .setSmallIcon(R.drawable.notification_icon) // Notification icon
                    .setContentIntent(contentIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Notification priority for better visibility
                    .setAutoCancel(true) // Dismiss notification when tapped
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // We use a layout id because it is a unique number.  We use it later to cancel.
            notificationManager.notify(NID_COUNTDOWN_DONE, notification);
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }
    }

    /**
     * 显示码表开始的通知
     *
     * @param context
     * @param elapsedTime
     */
    public static void notifyStopwatch(Context context, long elapsedTime) {
        // The PendingIntent to launch our activity if the user selects this notification
        Intent launcher = new Intent(context, UltimateStopwatchActivity.class);
        launcher.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launcher,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, UstopwatchConsts.NOTIFICATION_CHANNEL_START)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_start_sw))
                .setWhen(System.currentTimeMillis() - elapsedTime)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setUsesChronometer(true) // 这里开启了通知消息时间的自动更新
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificationManager.notify(NID_STOPWATCH_START, notification);
    }

    public static void cancelStopwatchNotification(Context context) {
        cancelNotification(context, NID_STOPWATCH_START);
    }

    private static void cancelNotification(Context context, int id) {
        try {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
        } catch (Exception ignored) {
        }
    }

    public static void notifyCountdown(Context context, long startTime) {
        // The PendingIntent to launch our activity if the user selects this notification
        Intent launcher = new Intent(context, UltimateStopwatchActivity.class);
        launcher.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.putExtra(INTENT_EXTRA_LAUNCH_COUNTDOWN, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launcher,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, UstopwatchConsts.NOTIFICATION_CHANNEL_COUNTDOWN)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_start_cd))
                .setWhen(System.currentTimeMillis() + startTime)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setUsesChronometer(true) // 这里开启了通知消息时间的自动更新
                .setChronometerCountDown(true)
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificationManager.notify(NID_COUNTDOWN_START, notification);
    }

    public static void cancelCountdownNotification(Context context) {
        cancelNotification(context, NID_COUNTDOWN_START);
    }
}
