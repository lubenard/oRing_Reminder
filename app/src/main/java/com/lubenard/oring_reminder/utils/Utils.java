package com.lubenard.oring_reminder.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;

import com.lubenard.oring_reminder.CurrentSessionWidgetProvider;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationReceiverBroadcastReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

    private static final String TAG = "Utils";

    private static String current_language;

    /**
     * Apply theme based on newValue
     * @param newValue the new Theme to apply
     */
    public static void applyTheme(String newValue) {
        switch (newValue) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "white":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "battery_saver":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * Apply language based on newValue
     * @param context context
     * @param newValue the new Language to apply
     */
    public static void applyLanguage(Context context, String newValue) {
        switch (newValue) {
            case "en":
                setAppLocale(context,"en-us");
                break;
            case "fr":
                setAppLocale(context, "fr");
                break;
            case "de":
                setAppLocale(context,"de");
                break;
            case "system":
            default:
                setAppLocale(context, Resources.getSystem().getConfiguration().locale.getLanguage());
                break;
        }
    }

    /**
     * Check if the input string is valid
     * @param text the given input string
     * @return 1 if the string is valid, else 0
     */
    //TODO: Refactor this method to make return boolean
    public static int checkDateInputSanity(String text) {
        if (text.equals("") || text.equals("NOT SET YET") || Utils.getdateParsed(text) == null)
            return 0;
        return 1;
    }

    /**
     * Format date from Date to string using "yyyy-MM-dd HH:mm:ss" format
     * @param date The date to format
     * @return The string formatted
     */
    public static String getdateFormatted(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * Parse a string date into a Date object
     * @param date the string date to parse
     * @return The Date format parsed
     */
    public static Date getdateParsed(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Compute the diff between two given dates
     * The formula is date2 - date1
     * @param sDate1 First date in the form of a string
     * @param sDate2 Second date in the form of a string
     * @param timeUnit The timeUnit we want to return (Mostly minutes)
     * @return the time in unit between two dates in the form of a long
     */
    public static long getDateDiff(String sDate1, String sDate2, TimeUnit timeUnit) {
        Date date1 = getdateParsed(sDate1);
        Date date2 = getdateParsed(sDate2);

        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * getDateDiff methord overload. Same as abovfe, but take 2 dates instead of 2 Strings
     * Compute the diff between two given dates
     * The formula is date2 - date1
     * @param date1 First date in the form of a Date
     * @param date2 Second date in the form of a Date
     * @param timeUnit The timeUnit we want to return (Mostly minutes)
     * @return the time in minutes between two dates
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }


    /**
     * Convert the timeWeared from a int into a readable hour:minutes format
     * @param timeWeared timeWeared is in minutes
     * @return a string containing the time the user weared the protection
     */
    public static String convertTimeWeared(int timeWeared) {
        return String.format("%dh%02dm", timeWeared / 60, timeWeared % 60);
    }


    /**
     * Convert date into readable one:
     * Example : 2021-10-12 -> 12 October 2021
     * @param s
     * @return
     */
    public static String convertDateIntoReadable(String s, boolean shorterVersion) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat simpleDateFormat;

        if (current_language == null)
            current_language = Resources.getSystem().getConfiguration().locale.getLanguage();

        if (shorterVersion)
            simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", new Locale(current_language));
        else
            simpleDateFormat = new SimpleDateFormat("dd LLLL yyyy", new Locale(current_language));

        return simpleDateFormat.format(date);
    }

    /**
     * Change language
     * @param context
     * @param localeCode localCode to apply
     */
    public static void setAppLocale(Context context, String localeCode) {
        current_language = localeCode;
        Locale myLocale = new Locale(localeCode);
        Resources res = context.getResources();
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
    public static void sendNotification(Context context, String title, String content, int drawable) {
        // First let's create the intent
        PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the notification manager and build it
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder permNotifBuilder = new NotificationCompat.Builder(context, "NORMAL_CHANNEL");
        permNotifBuilder.setSmallIcon(drawable)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pi);
        mNotificationManager.notify(0, permNotifBuilder.build());
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
        PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), getIntentMutableFlag());

        //Pending intent for a notification button when user removed protection
        PendingIntent removedProtection =
                PendingIntent.getBroadcast(context, 1, new Intent(context, NotificationReceiverBroadcastReceiver.class)
                                .putExtra("action", 1)
                                .putExtra("entryId", entryId),
                        getIntentMutableFlag());

        //Pending intent for a notification button when user dismissed notification
        PendingIntent dismissedNotif =
                PendingIntent.getBroadcast(context, 2, new Intent(context, NotificationReceiverBroadcastReceiver.class)
                                .putExtra("action", 0)
                                .putExtra("entryId", entryId),
                        getIntentMutableFlag());

        // Get the notification manager and build it
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder permNotifBuilder = new NotificationCompat.Builder(context, "NORMAL_CHANNEL");
        permNotifBuilder.setSmallIcon(drawable)
                .setContentTitle(title)
                .setContentText(content)
                .addAction(android.R.drawable.checkbox_on_background, context.getString(R.string.notif_choice_do_it), removedProtection)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.notif_choice_dismiss), dismissedNotif)
                .setContentIntent(pi);
        mNotificationManager.notify(0, permNotifBuilder.build());
    }

    /**
     * Instantly update the widget
     * @param context
     */
    public static void updateWidget(Context context) {
        //if (CurrentSessionWidgetProvider.isThereAWidget) {
        Log.d(TAG, "Updating Widget");
        Intent intent = new Intent(context, CurrentSessionWidgetProvider.class);
        context.sendBroadcast(intent);
        //}
    }

    /**
     * @return return the corresponding pending intent flag according to android version
     */
    public static int getIntentMutableFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        return PendingIntent.FLAG_UPDATE_CURRENT;
    }
}
