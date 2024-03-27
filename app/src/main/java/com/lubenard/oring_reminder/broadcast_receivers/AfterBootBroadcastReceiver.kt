package com.lubenard.oring_reminder.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.lubenard.oring_reminder.managers.DbManager
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager
import com.lubenard.oring_reminder.managers.SettingsManager
import com.lubenard.oring_reminder.utils.DateUtils
import com.lubenard.oring_reminder.utils.Log

import java.util.Calendar

/**
 * Start the app at boot, and re-set all alarms
 */
class AfterBootBroadcastReceiver: BroadcastReceiver() {

    val TAG: String = "AfterBootBroadcast"

    override fun onReceive(context: Context?, intent: Intent?) {
        // Set all alarms for running sessions, because they have been erased after reboot
        // Also called when user change time, and when app is updated
        val dbManager = DbManager(context)
        val settingsManager = SettingsManager(context)

        val userSettingWearingTime = settingsManager.getWearingTimeInt()
        val runningSessions = dbManager.getAllRunningSessions()

        runningSessions.forEach { session ->
            session.setBreakList(dbManager.getAllBreaksForId(session.getId(), false))
            // Do not set a alarm if session has a running pause, because we do not know when this pause is going
            // to end.
            // Only set a new alarm when the end time of the pause is known
            if (session.getIsInBreak()) {
                val calendar = Calendar.getInstance()
                calendar.setTime(DateUtils.getdateParsed(session.getStartDate()))
                calendar.add(Calendar.MINUTE, userSettingWearingTime + session.computeTotalTimePause())

                // Set alarms for session not finished
                Log.d(TAG, "(re) set alarm for session " + session.getId() + " at " + DateUtils.getdateFormatted(calendar.time))
                SessionsAlarmsManager.setAlarm(context, calendar, session.getId(), true)
            }
        }
    }
}
