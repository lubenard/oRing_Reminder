package com.lubenard.oring_reminder.utils;

import androidx.annotation.NonNull;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;

import java.util.HashMap;

public class SessionsUtils {
    /**
     * Compute percentage & color for progressBar
     * @param session The session used
     * @param wearingTimePref the user pref for time wearing in Minutes.
     * @return Hashmap containing <ProgressPercentage, ProgressColor>
     */
    @NonNull
    static public HashMap<Integer, Integer> computeProgressBarDatas(RingSession session, float wearingTimePref) {
        HashMap<Integer, Integer> progressDatas = new HashMap<>();
        int progressColor;
        int progressPercentage = (int) (session.getSessionDuration() / wearingTimePref * 100);

        if (session.getIsRunning()) {
            progressColor = R.color.yellow;
        } else {
            if (progressPercentage >= 100f) {
                progressColor = R.color.green_main_bar;
            }  else {
                progressColor = R.color.blue_main_bar;
            }
        }

        progressDatas.put(progressPercentage, progressColor);
        return progressDatas;
    }

    // TODO: Change behavior of this function.
    // Simplify [SessionsManager.computeTotalTimePause..] expression
    static public int computeTextColor(RingSession session, float wearingTimePref) {
        if (session.getIsRunning()) {
            return R.color.yellow;
        } else {
            if ((session.getTimeWorn() - session.computeTotalTimePause()) / 60 >= wearingTimePref)
                return android.R.color.holo_green_dark;
            else
                return android.R.color.holo_red_dark;
        }
    }
}