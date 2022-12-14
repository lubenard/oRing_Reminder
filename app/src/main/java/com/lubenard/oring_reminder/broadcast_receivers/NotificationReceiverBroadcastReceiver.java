package com.lubenard.oring_reminder.broadcast_receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.ui.fragments.EditEntryFragment;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;

public class NotificationReceiverBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra("action", 0);
        long entryId = intent.getLongExtra("entryId", 0);

        // If action == 1, set all session as finished
        // Else, do nothing
        if (action == 1) {
            if (entryId <= 0) {
                Toast.makeText(context, context.getString(R.string.toast_session_bad_id) + entryId, Toast.LENGTH_SHORT).show();
            } else {
                new DbManager(context).endSession(entryId);
                Toast.makeText(context, context.getString(R.string.toast_session_ended) + entryId, Toast.LENGTH_SHORT).show();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, 9);
                Toast.makeText(context, context.getString(R.string.you_can_get_it_on_again) +
                        Utils.getdateFormatted(calendar.getTime()), Toast.LENGTH_LONG).show();
                Utils.updateWidget(context);
            }
        }
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
    }
}
