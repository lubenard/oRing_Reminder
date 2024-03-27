package com.lubenard.oring_reminder.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.lubenard.oring_reminder.R
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager
import com.lubenard.oring_reminder.utils.Log

/**
 * Send notifs in case of too long break
 */
class NotificationSenderBreaksBroadcastReceiver: BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        // Launch a notification
        val action = intent?.getIntExtra("action", 0)
        if (context != null) {
            if (action == 1) {
                Log.d("NotificationSenderBroad", "Send notif if break is longer")
                SessionsAlarmsManager.sendNotification(context, context.getString(R.string.your_break_seems_longer_title),
                    context.getString(R.string.your_break_seems_longer_body),
                    android.R.drawable.ic_dialog_alert)
            }
        }
    }
}
