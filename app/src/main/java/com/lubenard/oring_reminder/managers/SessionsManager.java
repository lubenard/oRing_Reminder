package com.lubenard.oring_reminder.managers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.ui.fragments.EditEntryFragment;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
}
