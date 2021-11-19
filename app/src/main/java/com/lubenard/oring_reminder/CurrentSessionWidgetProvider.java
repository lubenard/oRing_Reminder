package com.lubenard.oring_reminder;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.ui.EditEntryFragment;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CurrentSessionWidgetProvider extends AppWidgetProvider {

    private static SharedPreferences sharedPreferences;
    private static DbManager dbManager;
    private static RemoteViews remoteViews;

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
        final int N = appWidgetIds.length;

        Log.d(TAG, "Updating widget");

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Intent intent = new Intent(context, MainActivity.class);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (dbManager == null)
                dbManager = new DbManager(context);

            RingSession lastEntry = dbManager.getLastRunningEntry();

            // If entering this condition, this mean a session is currently active
            if (lastEntry != null) {
                Log.d(TAG, "A current session has been found");

                Intent intent3 = new Intent(context, getClass());

                ArrayList<RingSession> session_breaks = dbManager.getAllPausesForId(dbManager.getLastRunningEntry().getId(), true);

                if (session_breaks.size() > 0) {
                    if (session_breaks.get(0).getIsRunning() == 1) {
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

                PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_button_start_stop_break_session, pendingIntent3);

                int totalTimePause = AfterBootBroadcastReceiver.computeTotalTimePause(dbManager, lastEntry.getId());
                long wornFor = Utils.getDateDiff(lastEntry.getDatePut(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                wornFor -= totalTimePause;

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Utils.getdateParsed(lastEntry.getDatePut()));
                calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")));

                int textResourceWhenGetItOff;

                long timeBeforeRemove = Utils.getDateDiff(Utils.getdateFormatted(new Date()), Utils.getdateFormatted(calendar.getTime()), TimeUnit.MINUTES);
                timeBeforeRemove += totalTimePause;

                if (timeBeforeRemove >= 0)
                    textResourceWhenGetItOff = R.string.in_about_entry_details;
                else {
                    textResourceWhenGetItOff = R.string.when_get_it_off_negative;
                    timeBeforeRemove *= -1;
                }

                remoteViews.setTextViewText(R.id.widget_date_from, lastEntry.getDatePut());
                remoteViews.setTextViewText(R.id.widget_worn_for, String.format("%dh%02dm", wornFor / 60, wornFor % 60));
                remoteViews.setTextViewText(R.id.widget_time_remaining, String.format(context.getString(textResourceWhenGetItOff), timeBeforeRemove / 60, timeBeforeRemove % 60));

                // Change 'Start session' button into 'Stop session'
                remoteViews.setTextViewText(R.id.widget_button_new_stop_session, context.getString(R.string.stop_active_session));

                // Action if user click on the button
                Intent intent2 = new Intent(context, getClass());
                intent2.setAction(WIDGET_BUTTON_STOP);

                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
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
                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_button_new_stop_session, pendingIntent2 );
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
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

    private void setBreakAlarm(Context context, String pauseBeginning, long entryId) {
        if (sharedPreferences.getBoolean("myring_prevent_me_when_pause_too_long", false)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Utils.getdateParsed(pauseBeginning));
            calendar.add(Calendar.MINUTE, sharedPreferences.getInt("myring_prevent_me_when_pause_too_long_date", 0));
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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "Widget receives OnRecieve command to update");
        Log.d(TAG, "intent action is " +  intent.getAction());

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CurrentSessionWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                // Clicked on the 'Start Session' button
                case WIDGET_BUTTON_START:
                    EditEntryFragment.setUpdateMainList(false);
                    new EditEntryFragment(context).insertNewEntry(Utils.getdateFormatted(new Date()), false);
                    break;
                // Clicked on the 'Stop Session' button
                case WIDGET_BUTTON_STOP:
                    dbManager.endSession(dbManager.getLastRunningEntry().getId());
                    break;
                // Clicked on the 'Start break' button
                case WIDGET_BUTTON_START_BREAK:
                    dbManager.createNewPause(dbManager.getLastRunningEntry().getId(), Utils.getdateFormatted(new Date()), "NOT SET YET", 1);
                    // Cancel alarm until breaks are set as finished.
                    // Only then set a new alarm date
                    Log.d(TAG, "Cancelling alarm for entry: " + dbManager.getLastRunningEntry().getId());
                    EditEntryFragment.cancelAlarm(context, dbManager.getLastRunningEntry().getId());
                    setBreakAlarm(context, Utils.getdateFormatted(new Date()), dbManager.getLastRunningEntry().getId());
                    break;
                // Clicked on the 'Stop break' button
                case WIDGET_BUTTON_STOP_BREAK:
                    dbManager.endPause(dbManager.getLastRunningEntry().getId());
                    // Cancel the break notification if it is set as finished.
                    Intent intent4 = new Intent(context, NotificationSenderBreaksBroadcastReceiver.class).putExtra("action", 1);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent4, 0);
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
