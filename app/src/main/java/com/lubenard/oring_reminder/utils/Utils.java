package com.lubenard.oring_reminder.utils;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

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
    public static boolean isDateSane(String text) {
        if (text.equals("") || text.equals("NOT SET YET") || DateUtils.getdateParsed(text) == null)
            return false;
        return true;
    }

    /**
     * Change language
     * @param context
     * @param localeCode localCode to apply
     */
    public static void setAppLocale(Context context, String localeCode) {
        DateUtils.setAppLocale(localeCode);
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

    public static void hideKbd(Context context, IBinder v) {
        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v,0);
    }
}
