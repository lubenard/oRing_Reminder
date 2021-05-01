package com.lubenard.oring_reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.ui.MainFragment;
import com.lubenard.oring_reminder.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static DbManager dbManager;

    /**
     * Apply config at app startup
     */
    private void checkConfig() {
        String theme_option = sharedPreferences.getString("ui_theme", "dark");
        Utils.applyTheme(theme_option);

        String language_option = sharedPreferences.getString("ui_language", "system");
        Utils.applyLanguage(this, language_option);

        if (sharedPreferences.getString("myring_prevent_me_when_no_session_started_date", null) == null) {
            sharedPreferences.edit().putString("myring_prevent_me_when_no_session_started_date", "12:00").apply();
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
        dbManager = new DbManager(this);
        checkConfig();
        createNotifChannel();

        // Then switch to the main Fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(android.R.id.content, new MainFragment());
        fragmentTransaction.commit();
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.closeDb();
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
