package com.lubenard.oring_reminder.managers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SettingsManager {

    private SharedPreferences sharedPreferences;

    private String ui_home_action_fab;
    private String wearing_time;
    private String theme;
    private String language;
    private Boolean isNotifChannelCreated;
    private Boolean shouldPreventIfNoSessionStartedToday;
    private Boolean shouldPreventIfOneSessionAlreadyRunning;
    private String shouldPreventIfNoSessionStartedTodayDate;
    private Boolean shouldSendNotifWhenSessionIsOver;
    private Boolean shouldSendNotifWhenBreakTooLong;
    private int shouldSendNotifWhenBreakTooLongDate;

    public SettingsManager(Context context) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        reloadSettings();
    }

    public void reloadSettings() {
        theme = sharedPreferences.getString("ui_theme", "dark");
        language = sharedPreferences.getString("ui_language", "system");

        isNotifChannelCreated = sharedPreferences.getBoolean("has_notif_channel_created", false);

        ui_home_action_fab = sharedPreferences.getString("ui_action_on_plus_button", "default");

        wearing_time = sharedPreferences.getString("myring_wearing_time", "15");

        shouldPreventIfOneSessionAlreadyRunning = sharedPreferences.getBoolean("myring_prevent_me_when_started_session", true);

        shouldSendNotifWhenBreakTooLong = sharedPreferences.getBoolean("myring_prevent_me_when_pause_too_long", false);
        shouldSendNotifWhenBreakTooLongDate = sharedPreferences.getInt("myring_prevent_me_when_pause_too_long_date", 0);

        shouldPreventIfNoSessionStartedToday = sharedPreferences.getBoolean("myring_prevent_me_when_no_session_started_for_today", false);
        shouldPreventIfNoSessionStartedTodayDate = sharedPreferences.getString("myring_prevent_me_when_no_session_started_date", "Not set");

        shouldSendNotifWhenSessionIsOver = sharedPreferences.getBoolean("myring_send_notif_when_session_over", true);
    }

    public String getActionUIFab() {
        return ui_home_action_fab;
    }

    public String getWearingTime() {
        return wearing_time;
    }

    public int getWearingTimeInt() {
        return Integer.parseInt(wearing_time);
    }

    public String getTheme() {
        return theme;
    }

    public String getLanguage() {
        return language;
    }

    public boolean getIsNotifChannelCreated() {
        return isNotifChannelCreated;
    }

    public boolean getShouldSendNotifWhenSessionIsOver() {
        return shouldSendNotifWhenSessionIsOver;
    }

    public boolean getShouldSendNotifWhenBreakTooLong() {
        return shouldSendNotifWhenBreakTooLong;
    }

    public int getShouldSendNotifWhenBreakTooLongDate() {
        return shouldSendNotifWhenBreakTooLongDate;
    }

    public boolean getShouldPreventIfOneSessionAlreadyRunning() {
        return shouldPreventIfOneSessionAlreadyRunning;
    }

    public boolean getShouldPreventIfNoSessionStartedToday() {
        return shouldPreventIfNoSessionStartedToday;
    }

    public String getShouldPreventIfNoSessionStartedTodayDate() {
        return shouldPreventIfNoSessionStartedTodayDate;
    }

}
