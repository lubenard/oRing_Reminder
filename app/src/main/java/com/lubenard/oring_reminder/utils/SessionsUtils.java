package com.lubenard.oring_reminder.utils;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.custom_components.Session;

import java.util.Calendar;
import java.util.Date;
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
    static public Pair<Integer, Integer> computeProgressBarDatas(RingSession session, float wearingTimePref) {

        int progressColor;
        Log.d(TAG, "session getSessionDuration is at " + session.getSessionDuration() + " wearingTimePref: " + wearingTimePref);
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

        Pair<Integer, Integer> progressDatas = new Pair<>(progressPercentage, progressColor);
        Log.d(TAG,"Computed progressBarDatas with: " + progressDatas);
        return progressDatas;
    }

    // TODO: Change behavior of this function.
    // Simplify [SessionsManager.computeTotalTimePause..] expression
    static public int computeTextColor(RingSession session, float wearingTimePref) {
        if (session.getStatus() == Session.SessionStatus.RUNNING) {
            return R.color.yellow;
        } else {
            if ((session.getSessionDuration() - session.computeTotalTimePause()) / 60 >= wearingTimePref)
                return android.R.color.holo_green_dark;
            else
                return android.R.color.holo_red_dark;
        }
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

    /**
     * Compute the estimated end (based on started date & session breaks)
     * @param session The session should include the breaks or the computeTotalTimePause will not take
     *                them into account
     * @return Calendar set on the time of estimated end
     */
    public static Calendar computeEstimatedEnd(RingSession session) {
        // Time is computed as:
        // number_of_hour_defined_in_settings + total_time_in_pause
        int estimatedEnd = MainActivity.getSettingsManager().getWearingTimeInt() + session.computeTotalTimePause();
        Log.d(TAG, "Estimated end is = " + estimatedEnd + " for session with id " + session.getId());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(session.getDatePutCalendar().getTime());
        calendar.add(Calendar.MINUTE, estimatedEnd);
        return calendar;
    }
}