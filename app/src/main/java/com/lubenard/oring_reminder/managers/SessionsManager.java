package com.lubenard.oring_reminder.managers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.ui.fragments.EditEntryFragment;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SessionsManager {

    private static final String TAG = "SessionManager";

    /**
     * Used by UI to check if a session has already started and if we should prevent the user.
     * If no session is already, running, save the entry into db.
     * @param formattedDatePut formatted using utils tools string from date
     */
    public static void insertNewEntry(Context context, String formattedDatePut) {

        DbManager dbManager = MainActivity.getDbManager();

        SettingsManager settingsManager = new SettingsManager(context);

        boolean warnUserAlreadyRunningSession = settingsManager.getShouldPreventIfOneSessionAlreadyRunning();
        HashMap<Integer, String> runningSessions = dbManager.getAllRunningSessions();

        if (!runningSessions.isEmpty() && warnUserAlreadyRunningSession) {
            new AlertDialog.Builder(context).setTitle(R.string.alertdialog_multiple_running_session_title)
                    .setMessage(R.string.alertdialog_multiple_running_session_body)
                    .setPositiveButton(R.string.alertdialog_multiple_running_session_choice1, (dialog, which) -> {
                        for (Map.Entry<Integer, String> sessions : runningSessions.entrySet()) {
                            Log.d(TAG, "Set session " + sessions.getKey() + " to finished");
                            dbManager.updateDatesRing(sessions.getKey(), sessions.getValue(), Utils.getdateFormatted(new Date()), 0);
                        }
                        saveEntry(context, formattedDatePut);
                    })
                    .setNegativeButton(R.string.alertdialog_multiple_running_session_choice2, (dialog, which) -> saveEntry(context, formattedDatePut))
                    .setIcon(android.R.drawable.ic_dialog_alert).show();
        } else {
            saveEntry(context, formattedDatePut);
        }
    }

    /**
     * Save entry into db
     * @param formattedDatePut DatePut
     */
    public static void saveEntry(Context context, String formattedDatePut) {
        DbManager dbManager = MainActivity.getDbManager();

        SettingsManager settingsManager = new SettingsManager(context);

        int weared_time = settingsManager.getWearingTimeInt();

        long newlyInsertedEntry = dbManager.createNewDatesRing(formattedDatePut, "NOT SET YET", 1);
        // Set alarm only for new entry
        if (settingsManager.getShouldSendNotifWhenSessionIsOver()) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Utils.getdateParsed(formattedDatePut));
                calendar.add(Calendar.HOUR_OF_DAY, weared_time);
                Log.d(TAG, "New entry: setting alarm at " + calendar.getTimeInMillis());
                SessionsAlarmsManager.setAlarm(context, Utils.getdateFormatted(calendar.getTime()), newlyInsertedEntry, false);
            }
    }

    /**
     * Start break on MainFragment
     */
    public static void startBreak(Context context) {

        DbManager dbManager = MainActivity.getDbManager();
        RingSession lastRunningEntry = dbManager.getLastRunningEntry();

        if (dbManager.getLastRunningPauseForId(lastRunningEntry.getId()) == null) {
            Log.d(TAG, "No running pause");
            dbManager.createNewPause(lastRunningEntry.getId(), Utils.getdateFormatted(new Date()), "NOT SET YET", 1);
            // Cancel alarm until breaks are set as finished.
            // Only then set a new alarm date
            Log.d(TAG, "Cancelling alarm for entry: " + lastRunningEntry.getId());
            SessionsAlarmsManager.cancelAlarm(context, lastRunningEntry.getId());
            SessionsAlarmsManager.setBreakAlarm(context, Utils.getdateFormatted(new Date()), lastRunningEntry.getId());
            EditEntryFragment.updateWidget(context);
        } else {
            Log.d(TAG, "Error: Already a running pause");
            Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get the total time pause for one session
     * @param datePut The datetime the user put the protection
     * @param entryId the entry id of the session
     * @param dateRemoved The datetime the user removed the protection
     * @return the total time in Minutes of new wearing time
     */
    public static int getWearingTimeWithoutPause(String datePut, long entryId, String dateRemoved) {
        long oldTimeBeforeRemove;
        int newValue;
        long totalTimePause;

        if (dateRemoved == null)
            oldTimeBeforeRemove = Utils.getDateDiff(datePut, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
        else
            oldTimeBeforeRemove = Utils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES);

        totalTimePause = computeTotalTimePause(MainActivity.getDbManager(), entryId);
        newValue = (int) (oldTimeBeforeRemove - totalTimePause);
        return Math.max(newValue, 0);
    }

    /**
     * Compute the total pause time by adding each one
     * @param dbManager dbManager
     * @param entryId entry to check
     * @return the int value of all pause time in minutes
     */
    public static int computeTotalTimePause(DbManager dbManager, long entryId) {
        ArrayList<RingSession> allPauses = dbManager.getAllPausesForId(entryId, false);
        int totalTimePause = 0;
        for (int i = 0; i != allPauses.size(); i++) {
            if (!allPauses.get(i).getIsRunning())
                totalTimePause += allPauses.get(i).getTimeWeared();
            else
                totalTimePause += Utils.getDateDiff(allPauses.get(i).getDateRemoved(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
        }
        return totalTimePause;
    }

    /**
     * Check if given session have running pause ongoing
     * @param dbManager dbManager
     * @param entryId entry to check
     * @return true if running break has been found, else false
     */
    public static boolean doesSessionHaveRunningPause(DbManager dbManager, long entryId) {
        ArrayList<RingSession> allPauses = dbManager.getAllPausesForId(entryId, false);
        for (int i = 0; i != allPauses.size(); i++) {
            if (allPauses.get(i).getIsRunning())
                return true;
        }
        return false;
    }
}