package com.lubenard.oring_reminder.utils;

import androidx.annotation.NonNull;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.custom_components.Session;
import com.lubenard.oring_reminder.managers.DbManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SessionsUtils {
    final static String TAG = "SessionsUtils";


    /**
     * Compute percentage & color for progressBar
     * @param session The session used
     * @param wearingTimePref the user pref for time wearing in Minutes.
     * @return Hashmap containing 'ProgressPercentage, ProgressColor'
     */
    @NonNull
    static public HashMap<Integer, Integer> computeProgressBarDatas(RingSession session, float wearingTimePref) {
        HashMap<Integer, Integer> progressDatas = new HashMap<>();
        int progressColor;
        int progressPercentage = (int) (session.getSessionDuration() / wearingTimePref * 100);

        if (progressPercentage < 1f)
            progressPercentage = 1;

        if (session.getStatus() == Session.SessionStatus.RUNNING) {
            progressColor = R.color.yellow;
        } else {
            if (progressPercentage >= 100f) {
                progressColor = R.color.green_main_bar;
            }  else {
                progressColor = R.color.red;
            }
        }

        progressDatas.put(progressPercentage, progressColor);
        return progressDatas;
    }

    // TODO: Change behavior of this function.
    // Simplify [SessionsManager.computeTotalTimePause..] expression
    static public int computeTextColor(RingSession session, float wearingTimePref) {
        if (session.getStatus() == Session.SessionStatus.RUNNING) {
            return R.color.yellow;
        } else {
            if ((session.getRingSessionDuration() - session.computeTotalTimePause()) / 60 >= wearingTimePref)
                return android.R.color.holo_green_dark;
            else
                return android.R.color.holo_red_dark;
        }
    }

    public static int computeTotalTimePause(DbManager dbManager, long entryId) {
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

    /**
     * Compute when user get it off according to breaks.
     * If the user made a 1h30 break, then he should wear it 1h30 more
     */
    public static long computeWornTime(RingSession session) {
        long timeWorn;
        long wornTime;
        long totalTimePause;

        // If session is running,
        // OldTimeWeared is the time in minute between the starting of the entry and the current Date
        // Else, oldTimeWeared is the time between the start of the entry and it's pause
        if (session.getStatus() == Session.SessionStatus.RUNNING) {
            timeWorn = DateUtils.getDateDiff(session.getDatePutCalendar().getTime(), new Date(), TimeUnit.MINUTES);
        }
        else {
            timeWorn = DateUtils.getDateDiff(session.getStartDate(), session.getEndDate(), TimeUnit.MINUTES);
        }

        totalTimePause = session.computeTotalTimePause();

        Log.d(TAG, "oldTimeWeared is:" + timeWorn + " ,totalTimePause is:" + totalTimePause);

        // Avoid having more time pause than weared time
        if (totalTimePause > timeWorn)
            totalTimePause = timeWorn;

        wornTime = timeWorn - totalTimePause;
        Log.d(TAG, "Compute newWearingTime = " + timeWorn + " - " + totalTimePause + " = " + wornTime);
        return wornTime;
    }

    public static Calendar computeEstimatedEnd(RingSession session) {
        // Time is computed as:
        // number_of_hour_defined_in_settings + total_time_in_pause
        int newAlarmDate = MainActivity.getSettingsManager().getWearingTimeInt() + session.computeTotalTimePause();
        Log.d(TAG, "New alarm date = " + newAlarmDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(session.getDatePutCalendar().getTime());
        calendar.add(Calendar.MINUTE, newAlarmDate);
        return calendar;
    }
}