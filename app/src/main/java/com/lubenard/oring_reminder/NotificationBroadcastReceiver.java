package com.lubenard.oring_reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DbManager dbManager = new DbManager(context);
        Utils.sendNotification(context, "Reminder:",
                "You can take your protection off! ",
                android.R.drawable.checkbox_on_background);
    }
}
