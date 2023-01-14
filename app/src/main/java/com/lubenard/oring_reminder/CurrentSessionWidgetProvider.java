package com.lubenard.oring_reminder;

import static com.lubenard.oring_reminder.utils.Utils.getIntentMutableFlag;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CurrentSessionWidgetProvider extends AppWidgetProvider {

    private static SettingsManager settingsManager;
    private static DbManager dbManager;

    public static final String WIDGET_BUTTON_START = "com.lubenard.oring_reminder.WIDGET_BUTTON_START";
    public static final String WIDGET_BUTTON_STOP = "com.lubenard.oring_reminder.WIDGET_BUTTON_STOP";
    public static final String WIDGET_BUTTON_START_BREAK = "com.lubenard.oring_reminder.WIDGET_BUTTON_START_BREAK";
    public static final String WIDGET_BUTTON_STOP_BREAK = "com.lubenard.oring_reminder.WIDGET_BUTTON_STOP_BREAK";

    public static final String APPWIDGET_ENABLED = "android.appwidget.action.APPWIDGET_ENABLED";
    public static final String APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String APPWIDGET_DELETED = "android.appwidget.action.APPWIDGET_DELETED";
    public static final String APPWIDGET_UPDATE_OPTIONS = "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS";

    private static final String TAG = "Widget";

    public static boolean isThereAWidget = false;
    private static AlarmManager am;

    // Update the Widget datas
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.d(TAG, "Updating widget");

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Intent intent = new Intent(context, MainActivity.class);

            if (dbManager == null)
                dbManager = new DbManager(context);

            if (settingsManager == null)
                settingsManager = new SettingsManager(context);

            RingSession lastEntry = dbManager.getLastRunningEntry();

            // If entering this condition, this mean a session is currently active
            if (lastEntry != null) {
                Log.d(TAG, "A current session has been found");

                Intent intent3 = new Intent(context, getClass());

                ArrayList<BreakSession> session_breaks = dbManager.getAllBreaksForId(dbManager.getLastRunningEntry().getId(), true);

                if (session_breaks.size() > 0) {
                    if (session_breaks.get(0).getIsRunning()) {
                        remoteViews.setTextViewText(R.id.widget_button_start_stop_break_session, context.getString(R.string.widget_stop_break));
                        intent3.setAction(WIDGET_BUTTON_STOP_BREAK);
                    } else {
                        remoteViews.setTextViewText(R.id.widget_button_start_stop_break_session, context.getString(R.string.widget_start_break));
                        intent3.setAction(WIDGET_BUTTON_START_BREAK);
                    }
                } else {
                    remoteViews.setTextViewText(R.id.widget_button_start_stop_break_session, context.getString(R.string.widget_start_break));
                    intent3.setAction(WIDGET_BUTTON_START_BREAK);
                }

                // Set the 'Add break' button to visible
                remoteViews.setViewVisibility(R.id.widget_button_start_stop_break_session, View.VISIBLE);

                PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context, 0, intent3, getIntentMutableFlag());
                remoteViews.setOnClickPendingIntent(R.id.widget_button_start_stop_break_session, pendingIntent3);

                int totalTimePause = SessionsManager.computeTotalTimePause(dbManager, lastEntry.getId());
                long wornFor = DateUtils.getDateDiff(lastEntry.getDatePut(), DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                wornFor -= totalTimePause;

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(lastEntry.getDatePutCalendar().getTime());
                calendar.add(Calendar.HOUR_OF_DAY, settingsManager.getWearingTimeInt());

                int textResourceWhenGetItOff;

                long timeBeforeRemove = DateUtils.getDateDiff(DateUtils.getdateFormatted(new Date()), DateUtils.getdateFormatted(calendar.getTime()), TimeUnit.MINUTES);
                timeBeforeRemove += totalTimePause;

                if (timeBeforeRemove >= 0)
                    textResourceWhenGetItOff = R.string.in_about_entry_details;
                else {
                    textResourceWhenGetItOff = R.string.when_get_it_off_negative;
                    timeBeforeRemove *= -1;
                }

                String[] lastEntrySplitted = lastEntry.getDatePut().split(" ");

                String timeRemoval = context.getString(R.string.at) + DateUtils.getdateFormatted(calendar.getTime()).split(" ")[1];

                remoteViews.setTextViewText(R.id.widget_date_from, DateUtils.convertDateIntoReadable(lastEntrySplitted[0], true) + "\n" + lastEntrySplitted[1]);
                remoteViews.setTextViewText(R.id.widget_worn_for, String.format("%dh%02dm", wornFor / 60, wornFor % 60));
                remoteViews.setTextViewText(R.id.widget_time_remaining, String.format(context.getString(textResourceWhenGetItOff), timeBeforeRemove / 60, timeBeforeRemove % 60) + "\n"
                        + timeRemoval);

                // Change 'Start session' button into 'Stop session'
                remoteViews.setTextViewText(R.id.widget_button_new_stop_session, context.getString(R.string.stop_active_session));

                // Action if user click on the button
                Intent intent2 = new Intent(context, getClass());
                intent2.setAction(WIDGET_BUTTON_STOP);

                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, intent2, getIntentMutableFlag());
                remoteViews.setOnClickPendingIntent(R.id.widget_button_new_stop_session, pendingIntent2);

                intent.putExtra("switchToEntry", lastEntry.getId());
            } else {
                Log.d(TAG, "There is no current session");
                remoteViews.setViewVisibility(R.id.widget_button_start_stop_break_session, View.GONE);
                remoteViews.setTextViewText(R.id.widget_date_from, "");
                remoteViews.setTextViewText(R.id.widget_worn_for, context.getString(R.string.no_running_session));
                remoteViews.setTextViewText(R.id.widget_time_remaining, "");

                // Change 'Stop session' into 'Start new session'
                remoteViews.setTextViewText(R.id.widget_button_new_stop_session, context.getString(R.string.create_new_session));

                // Action if user click on the button
                Intent intent2 = new Intent(context, getClass());
                intent2.setAction(WIDGET_BUTTON_START);
                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, intent2, getIntentMutableFlag());
                remoteViews.setOnClickPendingIntent(R.id.widget_button_new_stop_session, pendingIntent2);
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, getIntentMutableFlag());
            remoteViews.setOnClickPendingIntent(R.id.widget_root_view, pendingIntent);

            // Update the widget view.
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        isThereAWidget = true;
        Log.d(TAG, "onEnabled is called");
        dbManager = new DbManager(context);
        Intent intent = new Intent(context, CurrentSessionWidgetProvider.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_MUTABLE);
        am = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
        // 6000 millis is one minute
        am.setInexactRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(), 60000, pendingIntent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        isThereAWidget = false;
        Log.d(TAG, "onDisabled is called");
        Intent intent = new Intent(context, CurrentSessionWidgetProvider.class);
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        // Cancel alarm manager
        if (am == null)
            am = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(mPendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "Widget receives OnRecieve command to update");
        Log.d(TAG, "intent action is " +  intent.getAction());

        if (dbManager == null)
            dbManager = new DbManager(context);

        if (settingsManager == null)
            settingsManager = new SettingsManager(context);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CurrentSessionWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                // Clicked on the 'Start Session' button
                case WIDGET_BUTTON_START:
                    SessionsManager.saveEntry(context, DateUtils.getdateFormatted(new Date()));
                    break;
                // Clicked on the 'Stop Session' button
                case WIDGET_BUTTON_STOP:
                    dbManager.endSession(dbManager.getLastRunningEntry().getId());
                    break;
                // Clicked on the 'Start break' button
                case WIDGET_BUTTON_START_BREAK:
                    dbManager.createNewPause(dbManager.getLastRunningEntry().getId(), DateUtils.getdateFormatted(new Date()), "NOT SET YET", 1);
                    // Cancel alarm until breaks are set as finished.
                    // Only then set a new alarm date
                    Log.d(TAG, "Cancelling alarm for entry: " + dbManager.getLastRunningEntry().getId());
                    SessionsAlarmsManager.cancelAlarm(context, dbManager.getLastRunningEntry().getId());
                    SessionsAlarmsManager.setBreakAlarm(context, DateUtils.getdateFormatted(new Date()), dbManager.getLastRunningEntry().getId());
                    break;
                // Clicked on the 'Stop break' button
                case WIDGET_BUTTON_STOP_BREAK:
                    long lastRunningSessionId = dbManager.getLastRunningEntry().getId();
                    dbManager.endPause(lastRunningSessionId);
                    // Cancel the break notification if it is set as finished.
                    Intent intent4 = new Intent(context, NotificationSenderBreaksBroadcastReceiver.class).putExtra("action", 1);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int)lastRunningSessionId, intent4, PendingIntent.FLAG_MUTABLE);
                    AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
                    am.cancel(pendingIntent);
                    break;
                case APPWIDGET_ENABLED:
                case APPWIDGET_UPDATE:
                case APPWIDGET_DELETED:
                case APPWIDGET_UPDATE_OPTIONS:
                    onUpdate(context, appWidgetManager, appWidgetIds);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + intent.getAction());
            }
        }
        onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
