package com.lubenard.oring_reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.ui.EditEntryFragment;
import com.lubenard.oring_reminder.ui.EntryDetailsFragment;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CurrentSessionWidgetProvider extends AppWidgetProvider {

    private static SharedPreferences sharedPreferences;
    private static DbManager dbManager;
    private static RemoteViews remoteViews;

    public static String WIDGET_BUTTON = "com.lubenard.oring_reminder.WIDGET_BUTTON";

    public static boolean isThereAWidget = false;
    private static AlarmManager am;

    // Update the Widget datas
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Intent intent = new Intent(context, MainActivity.class);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (dbManager == null)
                dbManager = new DbManager(context);

            RingModel lastEntry = dbManager.getLastRunningEntry();

            if (lastEntry != null) {
                // Set the 'Create session' button to invisible
                remoteViews.setViewVisibility(R.id.widget_button_new_session, View.GONE);

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

                intent.putExtra("switchToEntry", lastEntry.getId());
            } else {
                remoteViews.setTextViewText(R.id.widget_date_from, "");
                remoteViews.setTextViewText(R.id.widget_worn_for, context.getString(R.string.no_running_session));
                remoteViews.setViewVisibility(R.id.widget_button_new_session, View.VISIBLE);
                remoteViews.setTextViewText(R.id.widget_time_remaining, "");

                // Action if user click on the button
                Intent intent2 = new Intent(context, getClass());
                intent2.setAction(WIDGET_BUTTON);
                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_button_new_session, pendingIntent2 );
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_root_view, pendingIntent);

            // Update the widget view.
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        isThereAWidget = true;
        Log.d("Widget", "onEnabled is called");
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
        Log.d("Widget", "onDisabled is called");
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
        Log.d("Widget", "Widget receives OnRecieve command to update");

        Log.d("Widget", "intent action is " +  intent.getAction());
        if (WIDGET_BUTTON.equals(intent.getAction())) {
            EditEntryFragment.setUpdateMainList(false);
            new EditEntryFragment(context).insertNewEntry(Utils.getdateFormatted(new Date()), false);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CurrentSessionWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
