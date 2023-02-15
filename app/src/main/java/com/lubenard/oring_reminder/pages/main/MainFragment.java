package com.lubenard.oring_reminder.pages.main;

import android.os.Bundle;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.pages.calendar.CalendarFragment;
import com.lubenard.oring_reminder.pages.home.HomeFragment;
import com.lubenard.oring_reminder.pages.settings.SettingsFragment;
import com.lubenard.oring_reminder.utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lubenard.oring_reminder.R;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private BottomNavigationView bottom_navigation_view;
    private int bottomNavigationViewCurrentIndex = -1;
    private SettingsManager settingsManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreateView()");
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottom_navigation_view = view.findViewById(R.id.bottomNavigationView);

        Log.d(TAG, "onViewCreated()");

        settingsManager = MainActivity.getSettingsManager();
        bottomNavigationViewCurrentIndex = settingsManager.getBottomNavigationViewCurrentIndex();

        if (bottomNavigationViewCurrentIndex == -1) {
            // Avoid recreating new fragment each time, we record the current fragment
            settingsManager.setBottomNavigationViewCurrentIndex(R.id.bottom_nav_bar_home);

            Log.d(TAG, "No Fragment found in settingsManager, creating homeFragment");

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_fragment, HomeFragment.class, null).commit();
        } else {
            switch (bottomNavigationViewCurrentIndex) {
                case R.id.bottom_nav_bar_home:
                    settingsManager.setBottomNavigationViewCurrentIndex(R.id.bottom_nav_bar_home);
                    Log.d(TAG, "Launching HomeFragment");
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, HomeFragment.class, null).addToBackStack(null).commit();
                    break;
                case R.id.bottom_nav_bar_calendar:
                    settingsManager.setBottomNavigationViewCurrentIndex(R.id.bottom_nav_bar_calendar);
                    Log.d(TAG, "Launching CalendarFragment");
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, CalendarFragment.class, null).commit();
                    break;
                case R.id.bottom_nav_bar_settings:
                    settingsManager.setBottomNavigationViewCurrentIndex(R.id.bottom_nav_bar_settings);
                    Log.d(TAG, "Launching SettingsFragment");
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, SettingsFragment.class, null).commit();
                    break;
            }
        }

        bottom_navigation_view.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            Log.d(TAG, "id = " + id + ", bottomNavigationViewCurrentIndex = " + bottomNavigationViewCurrentIndex);
            if (settingsManager.getBottomNavigationViewCurrentIndex() == id) {
                Log.d(TAG, "id == bottomNavigationViewCurrentIndex");
                return true;
            }
            bottomNavigationViewCurrentIndex = id;
            switch (id) {
                case R.id.bottom_nav_bar_home:
                    // Navigate to settings screen
                    settingsManager.setBottomNavigationViewCurrentIndex(id);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, HomeFragment.class, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    break;
                case R.id.bottom_nav_bar_calendar:
                    // Navigate to settings screen
                    settingsManager.setBottomNavigationViewCurrentIndex(id);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, CalendarFragment.class, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    break;
                case R.id.bottom_nav_bar_settings:
                    // Navigate to settings screen
                    settingsManager.setBottomNavigationViewCurrentIndex(R.id.bottom_nav_bar_settings);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, SettingsFragment.class, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    break;
            }
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView() called");
        super.onDestroyView();
    }
}
