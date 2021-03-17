package com.lubenard.oring_reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationSenderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch a notification
        Utils.sendNotificationWithQuickAnswer(context, context.getString(R.string.notif_get_it_off_title),
                context.getString(R.string.notif_get_it_off_body),
                R.drawable.baseline_done_24, intent.getIntExtra("entryId", -1));
    }
}
