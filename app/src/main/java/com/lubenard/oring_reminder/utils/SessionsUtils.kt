package com.lubenard.oring_reminder.utils

import androidx.core.util.Pair

import com.lubenard.oring_reminder.MainActivity
import com.lubenard.oring_reminder.R
import com.lubenard.oring_reminder.custom_components.RingSession
import com.lubenard.oring_reminder.custom_components.Session

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class SessionsUtils {
    companion object {
        val TAG = "SessionsUtils"

        /**
         * Compute percentage & color for progressBar
         * @param session The session used
         * @param wearingTimePref the user pref for time wearing in Minutes.
         * @return Hashmap containing 'ProgressPercentage, ProgressColor'
         */
        fun computeProgressBarDatas(session: RingSession, wearingTimePref: Float): Pair<Int, Int> {
            val progressColor: Int
            Log.d(
                TAG,
                "session getSessionDuration is at ${session.getSessionDuration()} wearingTimePref: $wearingTimePref"
            )
            var progressPercentage = (session.getSessionDuration() / wearingTimePref * 100)

            if (progressPercentage < 1f)
                progressPercentage = 1F

            progressColor = if (session.status == Session.SessionStatus.RUNNING) {
                R.color.yellow
            } else {
                if (progressPercentage >= 100f) {
                    R.color.green_main_bar
                } else {
                    R.color.red
                }
            }

            val progressDatas: Pair<Int, Int> = Pair(progressPercentage.toInt(), progressColor)
            Log.d(TAG, "Computed progressBarDatas with: $progressDatas")
            return progressDatas
        }

        // TODO: Change behavior of this function.
        // Simplify [SessionsManager.computeTotalTimePause..] expression
        fun computeTextColor(session: RingSession, wearingTimePref: Float): Int {
            return if (session.status == Session.SessionStatus.RUNNING) {
                R.color.yellow
            } else {
                if ((session.getSessionDuration() - session.computeTotalTimePause()) / 60 >= wearingTimePref)
                    android.R.color.holo_green_dark
                else
                    android.R.color.holo_red_dark
            }
        }

        /**
         * Compute when user get it off according to breaks.
         * If the user made a 1h30 break, then he should wear it 1h30 more
         */
        fun computeWornTime(session: RingSession): Long {
            val timeWorn: Long
            val wornTime: Long
            var totalTimePause: Long

            // If session is running,
            // OldTimeWeared is the time in minute between the starting of the entry and the current Date
            // Else, oldTimeWeared is the time between the start of the entry and it's pause
            if (session.status == Session.SessionStatus.RUNNING) {
                timeWorn =
                    DateUtils.getDateDiff(session.datePutCalendar.time, Date(), TimeUnit.MINUTES)
            } else {
                timeWorn =
                    DateUtils.getDateDiff(session.startDate, session.endDate, TimeUnit.MINUTES)
            }

            totalTimePause = session.computeTotalTimePause().toLong()

            Log.d(TAG, "oldTimeWeared is: $timeWorn, totalTimePause is: $totalTimePause")

            // Avoid having more time pause than worn time
            if (totalTimePause > timeWorn)
                totalTimePause = timeWorn

            wornTime = timeWorn - totalTimePause
            Log.d(TAG, "Compute newWearingTime = $timeWorn - $totalTimePause = $wornTime")
            return wornTime
        }

        /**
         * Compute the estimated end (based on started date & session breaks)
         * @param session The session should include the breaks or the computeTotalTimePause will not take
         *                them into account
         * @return Calendar set on the time of estimated end
         */
        fun computeEstimatedEnd(session: RingSession): Calendar {
            // Time is computed as:
            // number_of_hour_defined_in_settings + total_time_in_pause
            val estimatedEnd = MainActivity.getSettingsManager()
                .getWearingTimeInt() + session.computeTotalTimePause()
            Log.d(TAG, "Estimated end is = " + estimatedEnd + " for session with id " + session.id)

            val calendar = Calendar.getInstance()
            calendar.time = session.datePutCalendar.time
            calendar.add(Calendar.MINUTE, estimatedEnd)
            return calendar
        }
    }
}