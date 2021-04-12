package com.lubenard.oring_reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.ui.MainFragment;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    /**
     * Apply config at app startup
     */
    private void checkConfig() {
        String theme_option = sharedPreferences.getString("ui_theme", "dark");
        switch (theme_option) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "white":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "battery_saver":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        String language_option = sharedPreferences.getString("ui_language", "system");
        switch (language_option) {
            case "en":
                Utils.setAppLocale(this, "en-us");
                break;
            case "fr":
                Utils.setAppLocale(this, "fr");
                break;
            case "system":
                Utils.setAppLocale(this, Resources.getSystem().getConfiguration().locale.getLanguage());
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                getSupportFragmentManager().popBackStackImmediate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check the UI config (Theme and language) and apply them
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkConfig();
        createNotifChannel();

        Intent intent = new Intent(this, CurrentSessionWidgetProvider.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
        // 6000 millis is one minute
        am.setInexactRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(), 60000, pendingIntent);

        // Then switch to the main Fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(android.R.id.content, new MainFragment());
        fragmentTransaction.commit();
    }

    /**
     * Create notif channel if no one exist
     */
    private void createNotifChannel() {
        if (!sharedPreferences.getBoolean("has_notif_channel_created", false)) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("NORMAL_CHANNEL",
                        getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(getString(R.string.notif_normal_channel_desc));
                // Do not show badge
                channel.setShowBadge(false);
                mNotificationManager.createNotificationChannel(channel);
                sharedPreferences.edit().putBoolean("has_notif_channel_created", true).apply();
            }
        }
    }
}
