package com.lubenard.oring_reminder.pages.settings;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBroadcastReceiver;
import com.lubenard.oring_reminder.managers.BackupRestoreManager;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.pages.about.AboutFragment;
import com.lubenard.oring_reminder.pages.debug.DebugFragment;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;

/**
 * Settings page.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";
    private static Activity activity;
    private FragmentManager fragmentManager;
    private SettingsManager settingsManager;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey);
        activity = requireActivity();
        fragmentManager = requireActivity().getSupportFragmentManager();
        settingsManager = MainActivity.getSettingsManager();

        ((AppCompatActivity)activity).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        activity.setTitle(R.string.action_settings);

        // Language change listener
        Preference language = findPreference("ui_language");
        language.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d(TAG, "Language value has changed for " + newValue);
            Utils.applyLanguage(getContext(), newValue.toString());
            return true;
        });

        // Theme change listener
        Preference theme = findPreference("ui_theme");
        theme.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d(TAG, "Theme value has changed for " + newValue);
            Utils.applyTheme(newValue.toString());
            restartActivity();
            return true;
        });

        // wearing_time preference click listener
        Preference wearing_time = findPreference("myring_wearing_time");
        wearing_time.setSummary(DateUtils.convertIntIntoReadableDate(settingsManager.getWearingTimeInt()));
        wearing_time.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d(TAG, "onPreferenceChangeListener: newValue is type of " + newValue.getClass().getName());
            if (((String)newValue).matches("\\d+") || ((String)newValue).matches("\\d+:\\d+")) {
                String[] splittedWearingTime = ((String)newValue).split(":");

                int hoursWearing = Integer.parseInt(splittedWearingTime[0]);

                if (hoursWearing < 13 || hoursWearing > 18) {
                    new AlertDialog.Builder(getContext()).setTitle(R.string.alertdialog_dangerous_wearing_time)
                            .setMessage(R.string.alertdialog_dangerous_wearing_body)
                            .setPositiveButton(android.R.string.yes, null)
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                }
                settingsManager.setWearingTime((String)newValue);
                return true;
            } else {
                Log.d(TAG, "Failed to parse Float: newValue is " + newValue);
                new AlertDialog.Builder(getContext()).setTitle(R.string.alertdialog_please_enter_digits_title)
                        .setMessage(R.string.alertdialog_please_enter_digits_body)
                        .setPositiveButton(android.R.string.yes, null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }
            return false;
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        Preference choosingAlarmIfNoSessionStarted = findPreference("myring_prevent_me_when_no_session_started_date");
        choosingAlarmIfNoSessionStarted.setEnabled(settingsManager.getShouldPreventIfNoSessionStartedToday());
        choosingAlarmIfNoSessionStarted.setSummary(getString(R.string.settings_around) + settingsManager.getShouldPreventIfNoSessionStartedTodayDate());

        // Boolean if prevented about session not started for the day preference click listener
        Preference optionAlarmIfNoSessionStarted = findPreference("myring_prevent_me_when_no_session_started_for_today");
        optionAlarmIfNoSessionStarted.setOnPreferenceChangeListener((preference, newValue) -> {
            choosingAlarmIfNoSessionStarted.setEnabled((boolean) newValue);
            Log.d(TAG, "Alarm if no session started set : " + newValue);
            if (!((boolean) newValue)) {
                Intent intent = new Intent(getContext(), NotificationSenderBroadcastReceiver.class)
                        .putExtra("action", 2);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
                AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                // We cancel the old repetitive alarm
                am.cancel(pendingIntent);
            } else {
                // Should have a else case
            }
            return true;
        });

        choosingAlarmIfNoSessionStarted.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final View customLayout = getLayoutInflater().inflate(R.layout.time_chooser, null);
            builder.setView(customLayout);
            TimePicker timePicker = customLayout.findViewById(R.id.time_picker);
            timePicker.setIs24HourView(true);
            String oldTime = settingsManager.getShouldPreventIfNoSessionStartedTodayDate();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                timePicker.setCurrentHour(Integer.parseInt(oldTime.split(":")[0]));
                timePicker.setCurrentMinute(Integer.parseInt(oldTime.split(":")[1]));
            } else {
                timePicker.setHour(Integer.parseInt(oldTime.split(":")[0]));
                timePicker.setMinute(Integer.parseInt(oldTime.split(":")[1]));
            }
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {

                int timePickerHour;
                int timePickerMinutes;

                String alarmTime;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    timePickerHour = timePicker.getCurrentHour();
                    timePickerMinutes = timePicker.getCurrentMinute();
                } else {
                    timePickerHour = timePicker.getHour();
                    timePickerMinutes = timePicker.getMinute();
                }

                alarmTime = String.format("%02d:%02d", timePickerHour, timePickerMinutes);

                sharedPreferences.edit().putString("myring_prevent_me_when_no_session_started_date", alarmTime).apply();
                choosingAlarmIfNoSessionStarted.setSummary(getString(R.string.settings_around) + alarmTime);

                Intent intent = new Intent(getContext(), NotificationSenderBroadcastReceiver.class)
                        .putExtra("action", 2);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
                AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                // We cancel the old repetitive alarm
                am.cancel(pendingIntent);

                Calendar calendar = Calendar.getInstance();
                    Log.d(TAG, "User set repetitive alarm at " + alarmTime);
                    // This stupid calendar does not work well with only setTime(...).
                    // It causes it to trigger multiple time the event
                    // This is stupid, but working
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, timePickerHour);
                    calendar.set(Calendar.MINUTE, timePickerMinutes);
                    am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            });
            builder.setNegativeButton(android.R.string.cancel,null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });

        Preference choosingAlarmIfPauseTooLong = findPreference("myring_prevent_me_when_pause_too_long_date");
        choosingAlarmIfPauseTooLong.setEnabled(settingsManager.getShouldSendNotifWhenBreakTooLong());
        choosingAlarmIfPauseTooLong.setSummary(getString(R.string.settings_around) +
                settingsManager.getShouldSendNotifWhenBreakTooLongDate() + getString(R.string.minute_with_M_uppercase));

        // Boolean if prevented about session not started for the day preference click listener
        Preference optionAlarmIfPauseTooLong = findPreference("myring_prevent_me_when_pause_too_long");
        optionAlarmIfPauseTooLong.setOnPreferenceChangeListener((preference, newValue) -> {
            choosingAlarmIfPauseTooLong.setEnabled((boolean) newValue);
            return true;
        });

        choosingAlarmIfPauseTooLong.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final View customLayout = getLayoutInflater().inflate(R.layout.prevent_me_when_pause_too_long, null);
            builder.setView(customLayout);
            EditText numberOfMinutes = customLayout.findViewById(R.id.editTextBreakTooLong);
            int oldTime = settingsManager.getShouldSendNotifWhenBreakTooLongDate();
            numberOfMinutes.setText(String.valueOf(oldTime));
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                int alarmTime = Integer.parseInt(numberOfMinutes.getText().toString());

                sharedPreferences.edit().putInt("myring_prevent_me_when_pause_too_long_date", alarmTime).apply();
                choosingAlarmIfPauseTooLong.setSummary(getString(R.string.settings_around) + alarmTime +
                        getString(R.string.minute_with_M_uppercase));
            });
            builder.setNegativeButton(android.R.string.cancel,null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });

        Preference exportXML = findPreference("datas_export_data_xml");
        exportXML.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.custom_backup_title_alertdialog);
            final View customLayout = getLayoutInflater().inflate(R.layout.custom_view_backup_dialog, null);
            builder.setView(customLayout);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                MainActivity.checkOrRequestPerm(getActivity(), getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE, () -> null, () -> {
                    Toast.makeText(getContext(), getString(R.string.no_access_to_storage), Toast.LENGTH_LONG).show();
                    return null;
                });

                Intent intent = new Intent(getContext(), BackupRestoreManager.class);
                intent.putExtra("mode", 1);

                boolean isDatasChecked =
                        ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_datas)).isChecked();
                boolean isSettingsChecked =
                        ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_settings)).isChecked();

                if (!isDatasChecked && !isSettingsChecked)
                    return;

                intent.putExtra("shouldBackupRestoreDatas", isDatasChecked);
                intent.putExtra("shouldBackupRestoreSettings", isSettingsChecked);
                startActivity(intent);
            });
            builder.setNegativeButton(android.R.string.cancel,null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });

        Preference exportCSV = findPreference("datas_export_data_csv");
        exportCSV.setOnPreferenceClickListener(preference -> {
            MainActivity.checkOrRequestPerm(getActivity(), getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE, () -> null, () -> {
                Toast.makeText(getContext(), getString(R.string.no_access_to_storage), Toast.LENGTH_LONG).show();
                return null;
            });
            Intent intent = new Intent(getContext(), BackupRestoreManager.class);
            intent.putExtra("mode", 3);
            startActivity(intent);
            return true;
        });

        Preference importXML = findPreference("datas_import_data_xml");
        importXML.setOnPreferenceClickListener(preference -> {
            MainActivity.checkOrRequestPerm(getActivity(), getContext(), Manifest.permission.READ_EXTERNAL_STORAGE, () -> null, () -> {
                Toast.makeText(getContext(), getString(R.string.no_access_to_storage), Toast.LENGTH_LONG).show();
                return null;
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.custom_restore_title_alertdialog);
            final View customLayout = getLayoutInflater().inflate(R.layout.custom_view_backup_dialog, null);

            ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_datas)).setText(R.string.custom_restore_alertdialog_save_datas);
            ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_settings)).setText(R.string.custom_restore_alertdialog_save_settings);

            builder.setView(customLayout);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getContext(), BackupRestoreManager.class);
                    intent.putExtra("mode", 2);

                    boolean isDatasChecked =
                            ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_datas)).isChecked();
                    boolean isSettingsChecked =
                            ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_settings)).isChecked();

                    if (!isDatasChecked && !isSettingsChecked)
                        return;

                    intent.putExtra("shouldBackupRestoreDatas", isDatasChecked);
                    intent.putExtra("shouldBackupRestoreSettings", isSettingsChecked);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(android.R.string.cancel,null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });

        // reset preference click listener
        Preference reset = findPreference("datas_erase_data");
        reset.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.settings_alertdialog_erase_title)
                    .setMessage(R.string.settings_alertdialog_erase_datas_body)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete DB
                            getContext().deleteDatabase(DbManager.getDBName());
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        });

        // debug menu preference click listener
        Preference debugMenu = findPreference("other_debug_menu");
        debugMenu.setOnPreferenceClickListener(preference -> {
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, new DebugFragment(), null)
                    .addToBackStack(null).commit();
            return true;
        });

        // useful links menu preference click listener
        Preference usefulLinks = findPreference("other_useful_links");
        usefulLinks.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.custom_useful_links);
            final View customLayout = getLayoutInflater().inflate(R.layout.useful_links_alertdialog, null);
            builder.setView(customLayout);
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });

        // feedback preference click listener
        Preference feedback = findPreference("other_feedback");
        feedback.setOnPreferenceClickListener(preference -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","escatrag@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "oRing - Reminder");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
            return true;
        });

        // licenses preference click listener
        Preference aboutLicenses = findPreference("other_about_licenses");
        aboutLicenses.setOnPreferenceClickListener(preference -> {
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, new AboutFragment(), null)
                    .addToBackStack(null).commit();
            return true;
        });
    }

    public static void restartActivity() {
        activity.recreate();
    }
}

