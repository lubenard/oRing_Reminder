package com.lubenard.oring_reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.ui.EditEntryFragment;
import com.lubenard.oring_reminder.utils.Utils;

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
        DbManager dbManager = new DbManager(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        HashMap<Integer, String> runningSessions = dbManager.getAllRunningSessions();
        Calendar calendar = Calendar.getInstance();

        for (Map.Entry<Integer, String> sessions : runningSessions.entrySet()) {
            // Thoses lines are only for log
            calendar.setTime(Utils.getdateParsed(sessions.getValue()));
            calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")));
            Log.d(TAG, "(re) set alarm for session " + sessions.getKey() + " at " + Utils.getdateFormatted(calendar.getTime()));

            // This is the real and only useful line here
            EditEntryFragment.setAlarm(context, sessions.getValue(), sessions.getKey(),
                    Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")));
        }
    }
}
