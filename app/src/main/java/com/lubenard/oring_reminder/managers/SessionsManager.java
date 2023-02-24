package com.lubenard.oring_reminder.managers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.BreakSession;
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

        HashMap<Integer, String> runningSessions = dbManager.getAllRunningSessions();

        if (!runningSessions.isEmpty()) {
            new AlertDialog.Builder(context).setTitle(R.string.alertdialog_multiple_running_session_title)
                    .setMessage(R.string.alertdialog_multiple_running_session_body)
                    .setPositiveButton(R.string.alertdialog_multiple_running_session_choice1, (dialog, which) -> {
                        for (Map.Entry<Integer, String> sessions : runningSessions.entrySet()) {
                            Log.d(TAG, "Set session " + sessions.getKey() + " to finished");
                            dbManager.updateDatesRing(sessions.getKey(), sessions.getValue(), DateUtils.getdateFormatted(new Date()), 0);
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

        long newlyInsertedEntry = dbManager.createNewEntry(formattedDatePut, "NOT SET YET", 1);
        // Set alarm only for new entry
        if (settingsManager.getShouldSendNotifWhenSessionIsOver()) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(DateUtils.getdateParsed(formattedDatePut));
                calendar.add(Calendar.HOUR_OF_DAY, weared_time);
                Log.d(TAG, "New entry: setting alarm at " + calendar.getTimeInMillis());
                SessionsAlarmsManager.setAlarm(context, calendar, newlyInsertedEntry, false);
            }
    }

    /**
     * Try to start given break for given session.
     * the method will try to check if the break is inserable in the session.
     * If the session could not be inserted, return false.
     * else return true.
     */
    public static boolean startBreak2(Context context, RingSession session, BreakSession breakSession, boolean isNewEntry) {
        DbManager dbManager = MainActivity.getDbManager();
        if (breakSession.getSessionId() == -1) {
            Log.w(TAG, "Error: Wrong breakSession parent ID !!");
            //Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (session.getStatus() == RingSession.SessionStatus.IN_BREAK && breakSession.getIsRunning()) {
            Log.w(TAG, "Error: Already in break !");
            Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
            return false;
        } else if (DateUtils.getDateDiff(session.getDatePut(), breakSession.getStartDate(), TimeUnit.SECONDS) <= 0) {
            Log.w(TAG, "Error: Start of pause < start of entry");
            Toast.makeText(context, context.getString(R.string.pause_beginning_to_small), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!breakSession.getIsRunning() && DateUtils.getDateDiff(session.getDatePut(), breakSession.getEndDate(), TimeUnit.SECONDS) <= 0) {
            Log.w(TAG, "Error: End of pause < start of entry");
            Toast.makeText(context, context.getString(R.string.pause_ending_too_small), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!breakSession.getIsRunning() && !session.getIsRunning() && DateUtils.getDateDiff(breakSession.getEndDate(), session.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
            Log.w(TAG, "Error: End of pause > end of entry");
            Toast.makeText(context, context.getString(R.string.pause_ending_too_big), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!session.getIsRunning() && DateUtils.getDateDiff(breakSession.getStartDate(), session.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
            Log.w(TAG, "Error: Start of pause > end of entry");
            Toast.makeText(context, context.getString(R.string.pause_starting_too_big), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            // Session can be inserted
            if (isNewEntry) {
                long id = dbManager.createNewPause(breakSession);
                Log.d(TAG, "New break with id: " + id + " has been successfully inserted");
            } else {
                long id = dbManager.updatePause(breakSession);
                Log.d(TAG, "Break with id: " + id + " has been successfully updated");
                long timeWorn = DateUtils.getDateDiff(breakSession.getStartDate(), breakSession.getEndDate(), TimeUnit.MINUTES);

                //pausesDatas.set(position, new RingSession((int)id, pauseEndingText, pauseBeginningText, isRunning, (int)timeWorn));
                // Cancel the break notification if it is set as finished.
                if (!breakSession.getIsRunning()) {
                    SessionsAlarmsManager.cancelBreakAlarm(context, breakSession.getId());
                } else {
                    SessionsAlarmsManager.cancelAlarm(context, session.getId());
                    SessionsAlarmsManager.setBreakAlarm(context, DateUtils.getdateFormatted(new Date()), breakSession.getId());
                    Utils.updateWidget(context);
                }
            }
            return true;
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
            dbManager.createNewPause(lastRunningEntry.getId(), DateUtils.getdateFormatted(new Date()), "NOT SET YET", 1);
            // Cancel alarm until breaks are set as finished.
            // Only then set a new alarm date
            Log.d(TAG, "Cancelling alarm for entry: " + lastRunningEntry.getId());
            SessionsAlarmsManager.cancelAlarm(context, lastRunningEntry.getId());
            SessionsAlarmsManager.setBreakAlarm(context, DateUtils.getdateFormatted(new Date()), lastRunningEntry.getId());
            Utils.updateWidget(context);
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
            oldTimeBeforeRemove = DateUtils.getDateDiff(datePut, DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES);
        else
            oldTimeBeforeRemove = DateUtils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES);

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
        ArrayList<BreakSession> allPauses = dbManager.getAllBreaksForId(entryId, false);
        int totalTimePause = 0;
        for (int i = 0; i != allPauses.size(); i++) {
            if (allPauses.get(i).getIsRunning())
                totalTimePause += DateUtils.getDateDiff(allPauses.get(i).getStartDateCalendar().getTime(), new Date(), TimeUnit.MINUTES);
            else
                totalTimePause += allPauses.get(i).getTimeRemoved();
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
        ArrayList<BreakSession> allPauses = dbManager.getAllBreaksForId(entryId, false);
        for (int i = 0; i != allPauses.size(); i++) {
            if (allPauses.get(i).getIsRunning())
                return true;
        }
        return false;
    }
}
