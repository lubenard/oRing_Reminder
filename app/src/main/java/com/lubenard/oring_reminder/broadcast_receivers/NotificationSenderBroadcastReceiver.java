package com.lubenard.oring_reminder.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.utils.Log;

import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.R;

/**
 * Broadcast receiver used to send notifs
 */
public class NotificationSenderBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationSenderBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch a notification
        int action = intent.getIntExtra("action", 0);
        if (action == 1) {
            SessionsAlarmsManager.sendNotificationWithQuickAnswer(context, context.getString(R.string.notif_get_it_off_title),
                    context.getString(R.string.notif_get_it_off_body),
                    R.drawable.baseline_done_24, intent.getLongExtra("entryId", -1));
        } else if (action == 2) {
            Log.d("NotificationSenderBroadcastReceiver", "Check if there is a session running");
            if (new DbManager(context).getAllRunningSessions().size() == 0) {
                SessionsAlarmsManager.sendNotification(context, context.getString(R.string.have_you_start_session_today_title),
                        context.getString(R.string.have_you_start_session_today_body),
                        android.R.drawable.ic_dialog_alert);
            }
        } else if (action == 0) {
            SessionsAlarmsManager.sendNotificationWithQuickAnswer(context, "This is a test notification",
                    "No entry is affected by this notification",
                    R.drawable.baseline_done_24, -1);
        }
    }
}
