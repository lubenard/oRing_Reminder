package com.lubenard.oring_reminder.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.ui.fragments.EditEntryFragment;
import com.lubenard.oring_reminder.utils.Utils;

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

    public void onReceive(Context context, Intent arg1) {
        // Set all alarms for running sessions, because they have been erased after reboot
        // Also called when user change time, and when app is updated
        DbManager dbManager = new DbManager(context);
        SettingsManager settingsManager = new SettingsManager(context);

        int userSettingWearingTime = settingsManager.getWearingTimeInt();
        HashMap<Integer, String> runningSessions = dbManager.getAllRunningSessions();
        Calendar calendar = Calendar.getInstance();

        for (Map.Entry<Integer, String> sessions : runningSessions.entrySet()) {
            // Do not set a alarm if session has a running pause, because we do not know when this pause is going
            // to end.
            // Only set a new alarm when the end time of the pause is known
            if (!SessionsManager.doesSessionHaveRunningPause(dbManager, sessions.getKey())) {
                calendar.setTime(Utils.getdateParsed(sessions.getValue()));
                calendar.add(Calendar.HOUR_OF_DAY, userSettingWearingTime);
                calendar.add(Calendar.MINUTE, SessionsManager.computeTotalTimePause(dbManager, sessions.getKey()));

                // Set alarms for session not finished
                Log.d(TAG, "(re) set alarm for session " + sessions.getKey() + " at " + Utils.getdateFormatted(calendar.getTime()));
                SessionsAlarmsManager.setAlarm(context, Utils.getdateFormatted(calendar.getTime()), sessions.getKey(), true);
            }
        }
    }
}
