package com.lubenard.oring_reminder.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.Session;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Start the app at boot, and re-set all alarms
 */
public class AfterBootBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "AfterBootBroadcast";

    private static int computeTotalTimePause(DbManager dbManager, long entryId) {
        ArrayList<BreakSession> allPauses = dbManager.getAllBreaksForId(entryId, false);
        int totalTimePause = 0;
        for (int i = 0; i != allPauses.size(); i++) {
            if (allPauses.get(i).getStatus() == Session.SessionStatus.RUNNING)
                totalTimePause += DateUtils.getDateDiff(allPauses.get(i).getStartDateCalendar().getTime(), new Date(), TimeUnit.MINUTES);
            else
                totalTimePause += allPauses.get(i).getSessionDuration();
        }
        return totalTimePause;
    }

    public void onReceive(Context context, Intent arg1) {
        // Set all alarms for running sessions, because they have been erased after reboot
        // Also called when user change time, and when app is updated
        DbManager dbManager = new DbManager(context);
        SettingsManager settingsManager = new SettingsManager(context);

        int userSettingWearingTime = settingsManager.getWearingTimeInt();
        HashMap<Integer, String> runningSessions = dbManager.getAllRunningSessions();

        for (Map.Entry<Integer, String> sessions : runningSessions.entrySet()) {
            // Do not set a alarm if session has a running pause, because we do not know when this pause is going
            // to end.
            // Only set a new alarm when the end time of the pause is known
            if (!SessionsManager.doesSessionHaveRunningPause(dbManager, sessions.getKey())) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(DateUtils.getdateParsed(sessions.getValue()));
                calendar.add(Calendar.MINUTE, userSettingWearingTime);
                calendar.add(Calendar.MINUTE, computeTotalTimePause(dbManager, sessions.getKey()));

                // Set alarms for session not finished
                Log.d(TAG, "(re) set alarm for session " + sessions.getKey() + " at " + DateUtils.getdateFormatted(calendar.getTime()));
                SessionsAlarmsManager.setAlarm(context, calendar, sessions.getKey(), true);
            }
        }
    }
}
