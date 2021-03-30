package com.lubenard.oring_reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.ui.MainFragment;
import com.lubenard.oring_reminder.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

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
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Check the UI config (Theme and language) and apply them
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkConfig();
        createNotifChannel();
        super.onCreate(savedInstanceState);

        // Then switch to the main Fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(android.R.id.content, new MainFragment());
        fragmentTransaction.commit();
    }

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
