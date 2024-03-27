package com.lubenard.oring_reminder.broadcast_receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.widget.Toast

import com.lubenard.oring_reminder.R
import com.lubenard.oring_reminder.managers.DbManager
import com.lubenard.oring_reminder.utils.DateUtils
import com.lubenard.oring_reminder.utils.Log
import com.lubenard.oring_reminder.utils.Utils

import java.util.Calendar

/**
 * Notification handler: Decide what action to take based on quick answer clicked by user
 */
class NotificationReceiverBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.getIntExtra("action", 0)
        val entryId = intent?.getLongExtra("entryId", 0)

        if (context != null) {
            // If action == 1, set all session as finished
            // Else, do nothing
            if (action == 1) {
                if (entryId!! <= 0) {
                    Toast.makeText(context, context.getString(R.string.toast_session_bad_id) + entryId, Toast.LENGTH_SHORT).show()
                } else {
                    DbManager(context).endSession(entryId)
                    Toast.makeText(context, context.getString(R.string.toast_session_ended) + entryId, Toast.LENGTH_SHORT).show()
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.HOUR_OF_DAY, 9)
                    Toast.makeText(context, context.getString(R.string.you_can_get_it_on_again) + DateUtils.getdateFormatted(calendar.time), Toast.LENGTH_LONG).show()
                    Utils.updateWidget(context)
                }
            }
            val mNotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancel(0)
        } else {
            // TODO: Improve this system
            Log.d("NotifReceiverBoradcast", "Context is null")
        }
    }
}
