package com.lubenard.oring_reminder.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lubenard.oring_reminder.R
import com.lubenard.oring_reminder.managers.DbManager
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager
import com.lubenard.oring_reminder.utils.Log

/**
 * Broadcast receiver used to send notifs
 */
class NotificationSenderBroadcastReceiver: BroadcastReceiver() {
    val TAG: String = "NotificationSenderBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        // Launch a notification
        val action = intent?.getIntExtra("action", 0)
        if (context != null) {
            if (action == 1) {
                SessionsAlarmsManager.sendNotificationWithQuickAnswer(
                    context, context.getString(R.string.notif_get_it_off_title),
                    context.getString(R.string.notif_get_it_off_body),
                    R.drawable.baseline_done_24, intent.getLongExtra("entryId", -1)
                )
            } else if (action == 2) {
                Log.d("NotificationSenderBroadcastReceiver", "Check if there is a session running")
                if (DbManager (context).getAllRunningSessions().size == 0) {
                    SessionsAlarmsManager.sendNotification(
                        context, context.getString(R.string.have_you_start_session_today_title),
                        context.getString(R.string.have_you_start_session_today_body),
                        android.R.drawable.ic_dialog_alert
                    )
                }
            } else if (action == 0) {
                SessionsAlarmsManager.sendNotificationWithQuickAnswer(
                    context, "This is a test notification",
                    "No entry is affected by this notification",
                    R.drawable.baseline_done_24, -1
                )
            }
        }
    }
}
