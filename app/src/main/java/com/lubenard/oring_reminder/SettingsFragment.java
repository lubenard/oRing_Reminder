package com.lubenard.oring_reminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;

import java.util.Locale;

/**
 * Settings page.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String TAG = "SettingsActivity";
    private static Activity activity;
    private Fragment thisFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey);
        activity = getActivity();

        thisFragment = this;
        // Language change listener
        final Preference language = findPreference("ui_language");
        language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d(TAG, "Language value has changed for " + newValue);
                switch (newValue.toString()) {
                    case "en":
                        Utils.setAppLocale(activity, "en-us");
                        break;
                    case "fr":
                        Utils.setAppLocale(activity, "fr");
                        break;
                    case "system":
                        break;
                }
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                return true;
            }
        });

        // Theme change listener
        final Preference theme = findPreference("ui_theme");
        theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d(TAG, "Theme value has changed for " + newValue);
                switch (newValue.toString()) {
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
                restartActivity();
                return true;
            }
        });

        // feedback preference click listener
        Preference wearing_time = findPreference("myring_wearing_time");
        wearing_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int newTimeWeared = Integer.parseInt(newValue.toString());
                if (newTimeWeared < 13 || newTimeWeared > 18) {
                    new AlertDialog.Builder(getContext()).setTitle(R.string.alertdialog_dangerous_wearing_time)
                            .setMessage(R.string.alertdialog_dangerous_wearing_body)
                            .setPositiveButton(android.R.string.yes, null)
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                }
                return true;
            }
        });

        // feedback preference click listener
        Preference debugMenu = findPreference("other_debug_menu");
        debugMenu.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new DebugFragment(), null)
                        .addToBackStack(null).commit();
                return true;
            }
        });

        // feedback preference click listener
        Preference feedback = findPreference("other_feedback");
        feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","escatrag@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "oRing - Reminder");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                return true;
            }
        });

        // feedback preference click listener
        Preference aboutLicenses = findPreference("other_about_licenses");
        aboutLicenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new AboutFragment(), null)
                        .addToBackStack(null).commit();
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.settings_toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                }
            });
        }
    }

    public void restartActivity() {
        activity.recreate();
    }
}

