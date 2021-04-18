package com.lubenard.oring_reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.ui.EditEntryFragment;
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

    public static int computeTotalTimePause(DbManager dbManager, long entryId) {
        ArrayList<RingModel> allPauses = dbManager.getAllPausesForId(entryId, false);
        int totalTimePause = 0;
        for (int i = 0; i != allPauses.size(); i++) {
            if (allPauses.get(i).getIsRunning() == 0)
                totalTimePause += allPauses.get(i).getTimeWeared();
            else
                totalTimePause += Utils.getDateDiff(allPauses.get(i).getDateRemoved(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
        }
        Log.d(TAG, "TotalTimePause is: " + totalTimePause);
        return totalTimePause;
    }

    public void onReceive(Context context, Intent arg1) {
        // Set all alarms for running sessions, because they have been erased after reboot
        // Also called when user change time, and when app is updated
        DbManager dbManager = new DbManager(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int userSettingWearingTime = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));
        HashMap<Integer, String> runningSessions = dbManager.getAllRunningSessions();
        Calendar calendar = Calendar.getInstance();

        for (Map.Entry<Integer, String> sessions : runningSessions.entrySet()) {
            calendar.setTime(Utils.getdateParsed(sessions.getValue()));
            calendar.add(Calendar.HOUR_OF_DAY, userSettingWearingTime);
            calendar.add(Calendar.MINUTE, computeTotalTimePause(dbManager, sessions.getKey()));

            // Set alarms for session not finished
            Log.d(TAG, "(re) set alarm for session " + sessions.getKey() + " at " + Utils.getdateFormatted(calendar.getTime()));
            EditEntryFragment.setAlarm(context, Utils.getdateFormatted(calendar.getTime()), sessions.getKey(), true);
        }
    }
}
