package com.lubenard.oring_reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch a notification
        Utils.sendNotification(context, context.getString(R.string.notif_get_it_off_title),
                context.getString(R.string.notif_get_it_off_body),
                android.R.drawable.checkbox_on_background);
    }
}
