package com.lubenard.oring_reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static long getDateDiff(String sDate1, String sDate2, TimeUnit timeUnit)
    {
        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate1);
            Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate2);

            long diffInMillies = date2.getTime() - date1.getTime();
            return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void sendNotification(Context context, String title, String content, int drawable) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("NORMAL_CHANNEL",
                    context.getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notif_normal_channel_desc));
            // Do not show badge
            channel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder permNotifBuilder = new NotificationCompat.Builder(context, "NORMAL_CHANNEL");
        // Set icon
        permNotifBuilder.setSmallIcon(drawable);
        // Set main notif name
        permNotifBuilder.setContentTitle(title);
        // Set more description of the notif
        permNotifBuilder.setContentText(content);
        // Do not show time on the notif
        //permNotifBuilder.setShowWhen(false);

        Intent intent = new Intent(context, SettingsFragment.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        permNotifBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, permNotifBuilder.build());
    }
}
