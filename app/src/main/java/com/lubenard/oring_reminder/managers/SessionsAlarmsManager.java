package com.lubenard.oring_reminder.managers;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationReceiverBroadcastReceiver;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBroadcastReceiver;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;


/**
 * Utils to set / cancel alarms
 */
public class SessionsAlarmsManager {

    private static final String TAG = "SessionsAlarmsManager";

    // Schedule Alarms
    /**
     * This will set a alarm that will trigger a notification at given date
     * @param calendarAlarmDate The date as a Calendar
     * @param entryId the id entry of the entry to update
     */
    public static void setAlarm(Context context, Calendar calendarAlarmDate, long entryId, boolean cancelOldAlarm) {
        // From the doc, just create the exact same intent, and cancel it.
        // https://developer.android.com/reference/android/app/AlarmManager.html#cancel(android.app.PendingIntent)
        Intent intent = new Intent(context, NotificationSenderBroadcastReceiver.class)
                .putExtra("action", (entryId == -1) ? 0 : 1)
                .putExtra("entryId", entryId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, Utils.getIntentMutableFlag());
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (cancelOldAlarm)
            am.cancel(pendingIntent);

        Log.d(TAG, "Setting alarm for " + DateUtils.getCalendarParsed(calendarAlarmDate));

        am.setAlarmClock(new AlarmManager.AlarmClockInfo(calendarAlarmDate.getTimeInMillis(), pendingIntent), pendingIntent);
    }

    /**
     * Only cancel alarm for given entryId
     */
    public static void cancelAlarm(Context context, long entryId) {
        // From the doc, just create the exact same intent, and cancel it.
        // https://developer.android.com/reference/android/app/AlarmManager.html#cancel(android.app.PendingIntent)
        Intent intent = new Intent(context, NotificationSenderBroadcastReceiver.class)
                .putExtra("action", 1)
                .putExtra("entryId", entryId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, Utils.getIntentMutableFlag());
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    /**
     * Add alarm if break is too long (only if break is running and option enabled in settings)
     * @param pauseBeginning
     */
    public static void setBreakAlarm(Context context, String pauseBeginning, long entryId) {
        SettingsManager settingsManager = new SettingsManager(context);

        if (settingsManager.getShouldSendNotifWhenBreakTooLong()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtils.getdateParsed(pauseBeginning));
            calendar.add(Calendar.MINUTE, settingsManager.getShouldSendNotifWhenBreakTooLongDate());
            Log.d(TAG, "Setting break alarm at " + DateUtils.getdateFormatted(calendar.getTime()));
            Intent intent = new Intent(context, NotificationSenderBreaksBroadcastReceiver.class)
                    .putExtra("action", 1);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            if (SDK_INT < Build.VERSION_CODES.M)
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            else
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancelBreakAlarm(Context context, long entryId) {
        Intent intent = new Intent(context, NotificationSenderBreaksBroadcastReceiver.class).putExtra("action", 1);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, PendingIntent.FLAG_MUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    // Actually display notification
     /**
     * Send a notification on the 'normal' channel
     * @param context current Context
     * @param title Notification title
     * @param content notification body
     * @param drawable drawable icon
     */
    public static void sendNotification(Context context, String title, String content, int drawable) {
        // First let's create the intent
        PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), Utils.getIntentMutableFlag());

        // Get the notification manager and build it
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context, MainActivity.NOTIF_CHANNEL_ID);
        notifBuilder.setSmallIcon(drawable)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);
        notificationManager.notify(0, notifBuilder.build());
    }

    /**
     * Send a notification on the 'normal' channel
     * @param context current Context
     * @param title Notification title
     * @param content notification body
     * @param drawable drawable icon
     */
    public static void sendNotificationWithQuickAnswer(Context context, String title, String content, int drawable, long entryId) {
        // First let's create the intent
        PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), Utils.getIntentMutableFlag());

        //Pending intent for a notification button when user removed protection
        PendingIntent removedProtection =
                PendingIntent.getBroadcast(context, 1, new Intent(context, NotificationReceiverBroadcastReceiver.class)
                                .putExtra("action", 1)
                                .putExtra("entryId", entryId),
                        Utils.getIntentMutableFlag());

        //Pending intent for a notification button when user dismissed notification
        PendingIntent dismissedNotif =
                PendingIntent.getBroadcast(context, 2, new Intent(context, NotificationReceiverBroadcastReceiver.class)
                                .putExtra("action", 0)
                                .putExtra("entryId", entryId),
                        Utils.getIntentMutableFlag());

        // Get the notification manager and build it
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder permNotifBuilder = new NotificationCompat.Builder(context, MainActivity.NOTIF_CHANNEL_ID);
        permNotifBuilder.setSmallIcon(drawable)
                .setContentTitle(title)
                .setContentText(content)
                .addAction(android.R.drawable.checkbox_on_background, context.getString(R.string.notif_choice_do_it), removedProtection)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.notif_choice_dismiss), dismissedNotif)
                .setContentIntent(pi);
        mNotificationManager.notify(0, permNotifBuilder.build());
    }
}
