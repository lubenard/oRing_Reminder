package com.lubenard.oring_reminder.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;
import java.util.Calendar;

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
        ArrayList<RingSession> runningSessions = dbManager.getAllRunningSessions();

        for (int i = 0; i < runningSessions.size(); i++) {
            RingSession session = runningSessions.get(i);
            session.setBreakList(dbManager.getAllBreaksForId(session.getId(), false));
            // Do not set a alarm if session has a running pause, because we do not know when this pause is going
            // to end.
            // Only set a new alarm when the end time of the pause is known
            if (session.getIsInBreak()) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(DateUtils.getdateParsed(session.getStartDate()));
                calendar.add(Calendar.MINUTE, userSettingWearingTime + session.computeTotalTimePause());

                // Set alarms for session not finished
                Log.d(TAG, "(re) set alarm for session " + session.getId() + " at " + DateUtils.getdateFormatted(calendar.getTime()));
                SessionsAlarmsManager.setAlarm(context, calendar, session.getId(), true);
            }
        }
    }
}
