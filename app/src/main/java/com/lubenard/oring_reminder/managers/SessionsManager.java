package com.lubenard.oring_reminder.managers;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.custom_components.Session;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
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

        ArrayList<RingSession> runningSessions = dbManager.getAllRunningSessions();

        if (!runningSessions.isEmpty()) {
            new AlertDialog.Builder(context).setTitle(R.string.alertdialog_multiple_running_session_title)
                    .setMessage(R.string.alertdialog_multiple_running_session_body)
                    .setPositiveButton(R.string.alertdialog_multiple_running_session_choice1, (dialog, which) -> {
                        for (int i = 0; i < runningSessions.size(); i++) {
                            RingSession session = runningSessions.get(i);
                            Log.d(TAG, "Set session " + session.getId() + " to finished");
                            dbManager.updateDatesRing(session.getId(), session.getStartDate(), DateUtils.getdateFormatted(new Date()), 0);
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
                calendar.add(Calendar.MINUTE, weared_time);
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
        else if (DateUtils.getDateDiff(session.getStartDate(), breakSession.getStartDate(), TimeUnit.SECONDS) <= 0) {
            Log.w(TAG, "Error: Start of pause < start of entry");
            Toast.makeText(context, context.getString(R.string.pause_beginning_to_small), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!(breakSession.getStatus() == Session.SessionStatus.RUNNING) && DateUtils.getDateDiff(session.getStartDate(), breakSession.getEndDate(), TimeUnit.SECONDS) <= 0) {
            Log.w(TAG, "Error: End of pause < start of entry");
            Toast.makeText(context, context.getString(R.string.pause_ending_too_small), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!(breakSession.getStatus() == Session.SessionStatus.RUNNING) && !(session.getStatus() == Session.SessionStatus.RUNNING) && DateUtils.getDateDiff(breakSession.getEndDate(), session.getEndDate(), TimeUnit.SECONDS) <= 0) {
            Log.w(TAG, "Error: End of pause > end of entry");
            Toast.makeText(context, context.getString(R.string.pause_ending_too_big), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!(session.getStatus() == Session.SessionStatus.RUNNING) && DateUtils.getDateDiff(breakSession.getStartDate(), session.getEndDate(), TimeUnit.SECONDS) <= 0) {
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
                if (!(breakSession.getStatus() == Session.SessionStatus.RUNNING)) {
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

        Log.d(TAG, "No running pause");
        dbManager.createNewPause(lastRunningEntry.getId(), DateUtils.getdateFormatted(new Date()), "NOT SET YET", 1);
        dbManager.updateDatesRing(lastRunningEntry.getId(), null, null, RingSession.SessionStatus.IN_BREAK.ordinal());
        // Cancel alarm until breaks are set as finished.
        // Only then set a new alarm date
        Log.d(TAG, "Cancelling alarm for entry: " + lastRunningEntry.getId());
        SessionsAlarmsManager.cancelAlarm(context, lastRunningEntry.getId());
        SessionsAlarmsManager.setBreakAlarm(context, DateUtils.getdateFormatted(new Date()), lastRunningEntry.getId());
        Utils.updateWidget(context);
    }

    /**
     * Compute all pause time into interval
     * @param entryId entry for the wanted session
     * @param date24HoursAgo oldest boundaries
     * @param dateNow interval newest boundaries
     * @return the time in Minutes of pauses between the interval
     */
    public static int computeTotalTimePauseForId(DbManager dbManager, long entryId, String date24HoursAgo, String dateNow) {
        ArrayList<BreakSession> pausesDatas = dbManager.getAllBreaksForId(entryId, true);
        int totalTimePause = 0;
        for (int i = 0; i < pausesDatas.size(); i++) {
            BreakSession currentBreak = pausesDatas.get(i);
            Log.d(TAG, "BreakSession is " + currentBreak);
            if (!(pausesDatas.get(i).getStatus() == Session.SessionStatus.RUNNING)) {
                if (DateUtils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) > 0 &&
                        DateUtils.getDateDiff(currentBreak.getEndDate(), dateNow, TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is added: " + pausesDatas.get(i).getStartDate());
                    totalTimePause += currentBreak.getSessionDuration();
                } else if (DateUtils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) <= 0 &&
                        DateUtils.getDateDiff(date24HoursAgo, currentBreak.getEndDate(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is between the born: " + DateUtils.getDateDiff(date24HoursAgo, currentBreak.getEndDate(), TimeUnit.SECONDS));
                    totalTimePause += DateUtils.getDateDiff(date24HoursAgo, currentBreak.getEndDate(), TimeUnit.MINUTES);
                }
            } else {
                if (DateUtils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running pause at index " + i + " is added: " + DateUtils.getDateDiff(currentBreak.getStartDate(), dateNow, TimeUnit.SECONDS));
                    totalTimePause += DateUtils.getDateDiff(currentBreak.getStartDate(), dateNow, TimeUnit.MINUTES);
                } else if (DateUtils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running pause at index " + i + " is between the born: " + DateUtils.getDateDiff(date24HoursAgo, DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimePause += DateUtils.getDateDiff(date24HoursAgo, DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                }
            }
        }
        return totalTimePause;
    }
}
