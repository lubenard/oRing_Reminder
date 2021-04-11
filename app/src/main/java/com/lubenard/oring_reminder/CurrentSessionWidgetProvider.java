package com.lubenard.oring_reminder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.ui.EntryDetailsFragment;
import com.lubenard.oring_reminder.ui.MainFragment;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CurrentSessionWidgetProvider extends AppWidgetProvider {

    private static SharedPreferences sharedPreferences;
    private static DbManager dbManager;
    private static RemoteViews remoteViews;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        dbManager = new DbManager(context);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            remoteViews.setOnClickPendingIntent(R.id.widget_root_view, pendingIntent);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            RingModel lastEntry = dbManager.getLastRunningEntry();

            if (lastEntry != null) {
                long wornFor = Utils.getDateDiff(lastEntry.getDatePut(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Utils.getdateParsed(lastEntry.getDatePut()));
                calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")));

                int texteRessourceWhenGetItOff;

                long timeBeforeRemove = Utils.getDateDiff(Utils.getdateFormatted(new Date()), Utils.getdateFormatted(calendar.getTime()), TimeUnit.MINUTES);

                if (timeBeforeRemove >= 0)
                    texteRessourceWhenGetItOff = R.string.in_about_entry_details;
                else {
                    texteRessourceWhenGetItOff = R.string.when_get_it_off_negative;
                    timeBeforeRemove *= -1;
                }

                remoteViews.setTextViewText(R.id.widget_date_from, lastEntry.getDatePut());
                remoteViews.setTextViewText(R.id.widget_worn_for, String.format("%dh%02dm", wornFor / 60, wornFor % 60));
                remoteViews.setTextViewText(R.id.widget_time_remaining, String.format(context.getString(texteRessourceWhenGetItOff), timeBeforeRemove / 60, timeBeforeRemove % 60));
            } else {
                remoteViews.setTextViewText(R.id.widget_date_from, context.getString(R.string.no_running_session));
                remoteViews.setTextViewText(R.id.widget_worn_for, context.getString(R.string.no_running_session));
                remoteViews.setTextViewText(R.id.widget_time_remaining, context.getString(R.string.no_running_session));
            }
            // Update the widget view.
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("Widget", "Widget receives OnRecieve command to update");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CurrentSessionWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
