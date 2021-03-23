package com.lubenard.oring_reminder;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationReceiverBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra("action", 0);
        long entryId = intent.getLongExtra("entryId", 0);

        Log.d("Test", "Extra is " + action);
        // If action == 1, set all session as finished
        // Else, do nothing
        if (action == 1) {
            if (entryId <= 0) {
                Toast.makeText(context, context.getString(R.string.toast_session_bad_id) + entryId, Toast.LENGTH_SHORT).show();
            } else {
                new DbManager(context).endSession(entryId);
                Toast.makeText(context, context.getString(R.string.toast_session_ended) + entryId, Toast.LENGTH_SHORT).show();
            }
        }
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
    }
}
