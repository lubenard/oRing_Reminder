package com.lubenard.oring_reminder.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Should be used for long click on launcher
 * TODO: Not working for now
 */
public class ShortcutManager extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Test notif", Toast.LENGTH_SHORT).show();
    }
}
