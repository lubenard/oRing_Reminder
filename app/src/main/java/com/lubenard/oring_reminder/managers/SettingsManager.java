package com.lubenard.oring_reminder.managers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.utils.Log;

public class SettingsManager {

    private SharedPreferences sharedPreferences;

    // Shared Preferences settings
    private String ui_home_action_fab;
    private String wearing_time;
    private String theme;
    private String language;
    private Boolean isNotifChannelCreated;
    private Boolean shouldPreventIfNoSessionStartedToday;
    private String shouldPreventIfNoSessionStartedTodayDate;
    private Boolean shouldSendNotifWhenSessionIsOver;
    private Boolean shouldSendNotifWhenBreakTooLong;
    private int shouldSendNotifWhenBreakTooLongDate;
    private Boolean isLoggingEnabled;

    // Current session settings
    private int bottomNavigationViewCurrentIndex = -1;

    private static final String TAG = "SettingsManager";

    public SettingsManager(Context context) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(TAG, "Init SettingsManager");
        reloadSettings();
    }

    public void reloadSettings() {
        Log.d(TAG, "(Re)loading settings");

        theme = sharedPreferences.getString("ui_theme", "dark");
        language = sharedPreferences.getString("ui_language", "system");

        isNotifChannelCreated = sharedPreferences.getBoolean("has_notif_channel_created", false);

        ui_home_action_fab = sharedPreferences.getString("ui_action_on_plus_button", "default");

        wearing_time = sharedPreferences.getString("myring_wearing_time", "15");
        Log.d(TAG, "New wearing time: " + wearing_time);

        shouldSendNotifWhenBreakTooLong = sharedPreferences.getBoolean("myring_prevent_me_when_pause_too_long", false);
        shouldSendNotifWhenBreakTooLongDate = sharedPreferences.getInt("myring_prevent_me_when_pause_too_long_date", 0);

        shouldPreventIfNoSessionStartedToday = sharedPreferences.getBoolean("myring_prevent_me_when_no_session_started_for_today", false);
        shouldPreventIfNoSessionStartedTodayDate = sharedPreferences.getString("myring_prevent_me_when_no_session_started_date", "Not set");

        shouldSendNotifWhenSessionIsOver = sharedPreferences.getBoolean("myring_send_notif_when_session_over", true);

        isLoggingEnabled = sharedPreferences.getBoolean("debug_is_logging_enabled", false);
    }

    public String getActionUIFab() { return ui_home_action_fab; }

    public void setWearingTime(String newWearingTime) {
        Log.d(TAG, "Set wearing time to " + newWearingTime);
        wearing_time = newWearingTime;
        sharedPreferences.edit().putString("myring_wearing_time", newWearingTime).apply();
    }

    /**
     Return wearing time in HOURS
     */
    public String getWearingTime() {
        return wearing_time;
    }

    /**
     Return wearing time in MINUTES
     */
    public int getWearingTimeInt() {
        int wearingTimeInMinutes = 0;
        if (wearing_time.contains(":")) {
            String[] splittedWearingTime = wearing_time.split(":");
            Log.d(TAG, "getting splittedWearingTimeLength " + splittedWearingTime.length);
            wearingTimeInMinutes += Integer.parseInt(splittedWearingTime[0]) * 60;
            wearingTimeInMinutes += Integer.parseInt(splittedWearingTime[1]);
        } else
            wearingTimeInMinutes += Integer.parseInt(wearing_time) * 60;

        return wearingTimeInMinutes;
    }

    public String getTheme() { return theme; }

    public String getLanguage() { return language; }

    public boolean getIsNotifChannelCreated() { return isNotifChannelCreated; }

    public boolean getShouldSendNotifWhenSessionIsOver() { return shouldSendNotifWhenSessionIsOver; }

    public boolean getShouldSendNotifWhenBreakTooLong() { return shouldSendNotifWhenBreakTooLong; }

    public int getShouldSendNotifWhenBreakTooLongDate() { return shouldSendNotifWhenBreakTooLongDate; }

    public boolean getShouldPreventIfNoSessionStartedToday() { return shouldPreventIfNoSessionStartedToday; }

    public String getShouldPreventIfNoSessionStartedTodayDate() { return shouldPreventIfNoSessionStartedTodayDate; }

    public void setBottomNavigationViewCurrentIndex(int newStatus) { bottomNavigationViewCurrentIndex = newStatus; }

    public int getBottomNavigationViewCurrentIndex() { return bottomNavigationViewCurrentIndex; }

    public void setIsLoggingEnabled(Boolean newValue) {
        isLoggingEnabled = newValue;
        Log.setIsLogEnabled(isLoggingEnabled);
        sharedPreferences.edit().putBoolean("debug_is_logging_enabled", isLoggingEnabled).apply();
    }

    public Boolean getIsLoggingEnabled() {
        return isLoggingEnabled;
    }

}
