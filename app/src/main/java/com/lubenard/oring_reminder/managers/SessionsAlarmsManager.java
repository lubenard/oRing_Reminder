package com.lubenard.oring_reminder.managers;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import com.lubenard.oring_reminder.utils.Log;

import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBroadcastReceiver;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;

public class SessionsAlarmsManager {

    private static final String TAG = "SessionsAlarmsManager";

    /**
     * Add alarm if break is too long (only if break is running and option enabled in settings)
     * @param pauseBeginning
     */
    public static void setBreakAlarm(Context context, String pauseBeginning, long entryId) {
        SettingsManager settingsManager = new SettingsManager(context);

        if (settingsManager.getShouldSendNotifWhenBreakTooLong()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Utils.getdateParsed(pauseBeginning));
            calendar.add(Calendar.MINUTE, settingsManager.getShouldSendNotifWhenBreakTooLongDate());
            Log.d(TAG, "Setting break alarm at " + Utils.getdateFormatted(calendar.getTime()));
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

    /**
     * This will set a alarm that will trigger a notification at alarmDate + time wearing setting
     * @param alarmDate The date of the alarm in the form 2020-12-30 10:42:00
     * @param entryId the id entry of the entry to update
     */
    public static void setAlarm(Context context, String alarmDate, long entryId, boolean cancelOldAlarm) {
        // From the doc, just create the exact same intent, and cancel it.
        // https://developer.android.com/reference/android/app/AlarmManager.html#cancel(android.app.PendingIntent)
        Intent intent = new Intent(context, NotificationSenderBroadcastReceiver.class)
                .putExtra("action", 1)
                .putExtra("entryId", entryId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, PendingIntent.FLAG_MUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (cancelOldAlarm)
            am.cancel(pendingIntent);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utils.getdateParsed(alarmDate));

        if (SDK_INT < Build.VERSION_CODES.M)
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        else
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, PendingIntent.FLAG_MUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }
}
