package com.geekyouup.android.ustopwatch.adapter;


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
import com.geekyouup.android.ustopwatch.SettingsActivity;
import com.geekyouup.android.ustopwatch.UltimateStopwatchActivity;
import com.geekyouup.android.ustopwatch.constant.UstopwatchConsts;

public class AlarmUpdater {

    public static final String INTENT_EXTRA_LAUNCH_COUNTDOWN = "launch_countdown";

    public static void cancelCountdownAlarm(Context context) {
        try {
            AlarmManager alarmMan = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent defineIntent = new Intent(context, UpdateService.class);
            defineIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent piWakeUp = PendingIntent.getService(context, 0, defineIntent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

            if (piWakeUp != null) alarmMan.cancel(piWakeUp);
        } catch (Exception ignored) {
        }

        try {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(R.layout.main);
        } catch (Exception ignored) {
        }
    }

    /**
     * cancels alarm then sets new one
     *
     * @param context
     * @param inMillis
     */
    public static void setCountdownAlarm(Context context, long inMillis) {
        AlarmManager alarmMan = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent defineIntent = new Intent(context, UpdateService.class);
        defineIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent piWakeUp = PendingIntent.getService(context, 0, defineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (inMillis != -1)
            alarmMan.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + inMillis, piWakeUp);
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

            try {
                notification.ledARGB = 0xFF808080;
                notification.ledOnMS = 500;
                notification.ledOffMS = 1000;
                if (SettingsActivity.isVibrate()) notification.vibrate = new long[]{1000};
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            } catch (Exception ignored) {
            }

            notification.defaults |= (Notification.DEFAULT_ALL);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // We use a layout id because it is a unique number.  We use it later to cancel.
            notificationManager.notify(R.layout.main, notification);
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
     * @param startTime
     */
    public static void showChronometerNotification(Context context, long startTime) {
        // The PendingIntent to launch our activity if the user selects this notification
        Intent launcher = new Intent(context, UltimateStopwatchActivity.class);
        launcher.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launcher,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, UstopwatchConsts.NOTIFICATION_CHANNEL_START)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_start_sw))
                .setWhen(System.currentTimeMillis() - startTime)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setUsesChronometer(true) // 这里开启了通知消息时间的自动更新
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificationManager.notify(R.layout.stopwatch_fragment, notification);
    }

    public static void cancelChronometerNotification(Context context) {
        try {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(R.layout.stopwatch_fragment);
        } catch (Exception ignored) {
        }
    }
}
