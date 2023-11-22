package com.lubenard.oring_reminder;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.pages.entry_details.EntryDetailsFragment;
import com.lubenard.oring_reminder.pages.home.HomeViewModel;
import com.lubenard.oring_reminder.pages.main.MainFragment;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String NOTIF_CHANNEL_ID = "NORMAL_CHANNEL";
    private SharedPreferences sharedPreferences;
    private static SettingsManager settingsManager;
    private static DbManager dbManager;
    private static Log logManager;

    private static Callable onPermissionSuccess;
    private static Callable onPermissionError;

    /**
     * Check the requested permission, and if not already gave, ask for it
     * @param permRequired
     * @return
     */
    public static boolean checkOrRequestPerm(Activity activity, Context context, String permRequired, Callable onSucess, Callable onError) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, permRequired) == PackageManager.PERMISSION_GRANTED) {
                try {
                    onSucess.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permRequired)) {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                try {
                    onError.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                onPermissionSuccess = onSucess;
                onPermissionError = onError;
                activity.requestPermissions(new String[]{permRequired}, 1);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow
                // in your app.
                try {
                    onPermissionSuccess.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Explain to the user that the feature is unavailable because
                // the features requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
                try {
                    onPermissionError.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Apply config at app startup
     */
    private void applyUserConfig() {
        String theme = settingsManager.getTheme();
        Utils.applyTheme(theme);

        String language_option = settingsManager.getLanguage();
        Utils.applyLanguage(this, language_option);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate()");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Init log module
        logManager = new Log(this, sharedPreferences.getBoolean("debug_is_logging_enabled", false));

        // Check the UI config (Theme and language) and apply them
        settingsManager = new SettingsManager(this);
        dbManager = new DbManager(this);

        //initViewModels();

        applyUserConfig();
        createNotifChannel();
        askPermissions();
        // Create dynamical quick shortcut
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
        //    createQuickShortcut();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Intent intent = getIntent();
        if (intent.getLongExtra("switchToEntry", -1) != -1) {
            Log.d("Widget", "Opening for given session id : " + intent.getLongExtra("switchToEntry", -1));
            Bundle bundle = new Bundle();
            bundle.putLong("entryId", intent.getLongExtra("switchToEntry", -1));
            Fragment fragment = new EntryDetailsFragment();
            fragment.setArguments(bundle);
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else {
            Log.d("Widget", "No given session id");
            // Then switch to the main Fragment
            fragmentTransaction.replace(android.R.id.content, new MainFragment());
        }
        fragmentTransaction.commit();
    }

    private void askPermissions() {
        if (!settingsManager.getIsNotifPermGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkOrRequestPerm(this, this, Manifest.permission.SCHEDULE_EXACT_ALARM, () -> null, () -> null);
            checkOrRequestPerm(this, this, Manifest.permission.POST_NOTIFICATIONS, () -> {
                sharedPreferences.edit().putBoolean("is_notif_perm_granted", true).apply();
                return null;
            }, () -> {
                Toast.makeText(this, getString(R.string.no_access_to_notif), Toast.LENGTH_LONG).show();
                return null;
            });
        }
    }

    /**
     * Not used for now
     */
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private void createQuickShortcut() {
        String dynamicIntent = "com.lubenard.oring_reminder.android.action.broadcast";

        IntentFilter intentFilter = new IntentFilter(dynamicIntent);
        registerReceiver(new com.lubenard.oring_reminder.managers.ShortcutManager(), intentFilter);

        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        ShortcutInfo manageSessionShortcut = new ShortcutInfo.Builder(this, "manage_session")
                .setShortLabel("Create new session")
                .setLongLabel("Create a real new session")
                .setIcon(Icon.createWithResource(this, R.drawable.baseline_add_green_48))
                //.setIntent(new Intent(dynamicIntent))
                .build();
        //shortcutManager.setDynamicShortcuts(Arrays.asList(manageSessionShortcut));
    }

    public static DbManager getDbManager() {
        return dbManager;
    }
    public static SettingsManager getSettingsManager() {
        return settingsManager;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy()");
        dbManager.closeDb();
        logManager.closeFile();
        super.onDestroy();
    }

    /**
     * Create notif channel if no one exist
     */
    private void createNotifChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID,
                        getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(getString(R.string.notif_normal_channel_desc));
                // Do not show badge
                channel.setShowBadge(false);
                notificationManager.createNotificationChannel(channel);
        }
    }
}
