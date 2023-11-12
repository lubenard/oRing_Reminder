package com.lubenard.oring_reminder.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Start the app at boot, and re-set all alarms
 */
public class AfterBootBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "AfterBootBroadcast";

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
