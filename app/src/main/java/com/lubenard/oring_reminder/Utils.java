package com.lubenard.oring_reminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

    /**
     * Compute the diff between two given dates
     * The formula is date2 - date1
     * @param sDate1 First date in the form of a string
     * @param sDate2 Second date in the form of a string
     * @param timeUnit The timeUnit we want to return (Mostly minutes)
     * @return the time in minutes between two dates
     */
    public static long getDateDiff(String sDate1, String sDate2, TimeUnit timeUnit)
    {
        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate1);
            Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate2);

            long diffInMillies = date2.getTime() - date1.getTime();
            return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Set App language
     * @param activity current actvity
     * @param localeCode the locale we want to apply (ex: fr)
     */
    static void setAppLocale(Activity activity, String localeCode) {
        Locale myLocale = new Locale(localeCode);
        Resources res = activity.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    /**
     * Send a notification on the 'normal' channel
     * @param context current Context
     * @param title Notification title
     * @param content notification body
     * @param drawable drawable icon
     */
    public static void sendNotificationWithQuickAnswer(Context context, String title, String content, int drawable, int entryId) {
        // First let's create the intent
        PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        //Pending intent for a notification button when user removed protection
        PendingIntent removedProtection =
                PendingIntent.getBroadcast(context, 1, new Intent(context, NotificationReceiverBroadcastReceiver.class)
                                .putExtra("action", 1)
                                .putExtra("entryId", entryId),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        //Pending intent for a notification button when user dismissed notification
        PendingIntent dismissedNotif =
                PendingIntent.getBroadcast(context, 2, new Intent(context, NotificationReceiverBroadcastReceiver.class)
                                .putExtra("action", 0)
                                .putExtra("entryId", entryId),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the notification manager and build it
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder permNotifBuilder = new NotificationCompat.Builder(context, "NORMAL_CHANNEL");
        permNotifBuilder.setSmallIcon(drawable)
                .setContentTitle(title)
                .setContentText(content)
                .addAction(android.R.drawable.checkbox_on_background, "Okay, i did it", removedProtection)
                .addAction(android.R.drawable.checkbox_on_background, "Dismiss", dismissedNotif)
                .setContentIntent(pi);
        mNotificationManager.notify(0, permNotifBuilder.build());
    }
}
