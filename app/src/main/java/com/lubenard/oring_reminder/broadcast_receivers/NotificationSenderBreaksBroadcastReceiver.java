package com.lubenard.oring_reminder.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.lubenard.oring_reminder.utils.Log;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.utils.Utils;

public class NotificationSenderBreaksBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch a notification
        int action = intent.getIntExtra("action", 0);
        if (action == 1) {
            Log.d("NotificationSenderBroad", "Send notif if break is longer");
            Utils.sendNotification(context, context.getString(R.string.your_break_seems_longer_title),
                    context.getString(R.string.your_break_seems_longer_body),
                    android.R.drawable.ic_dialog_alert);
        }
    }
}
