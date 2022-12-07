package com.lubenard.oring_reminder.ui.fragments;

import android.os.Bundle;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.managers.SettingsManager;
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
    private HomeFragment homeFragment = null;
    private CalendarFragment calendarFragment = null;
    private SettingsFragment settingsFragment = null;
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

            homeFragment = new HomeFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_fragment, homeFragment, null).addToBackStack(null).commit();
        } else {
            switch (bottomNavigationViewCurrentIndex) {
                case R.id.bottom_nav_bar_home:
                    settingsManager.setBottomNavigationViewCurrentIndex(R.id.bottom_nav_bar_home);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, new HomeFragment(), null).addToBackStack(null).commit();
                    break;
                case R.id.bottom_nav_bar_calendar:
                    settingsManager.setBottomNavigationViewCurrentIndex(R.id.bottom_nav_bar_calendar);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, new CalendarFragment(), null).addToBackStack(null).commit();
                    break;
                case R.id.bottom_nav_bar_settings:
                    settingsManager.setBottomNavigationViewCurrentIndex(R.id.bottom_nav_bar_settings);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, new SettingsFragment(), null).addToBackStack(null).commit();
                    break;
            }
        }

        bottom_navigation_view.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            Log.d(TAG, "id = " + id + ", bottomNavigationViewCurrentIndex = " + bottomNavigationViewCurrentIndex);
            if (settingsManager.getBottomNavigationViewCurrentIndex() == id) {
                Log.d(TAG, "id = bottomNavigationViewCurrentIndex");
                return true;
            }
            switch (id) {
                case R.id.bottom_nav_bar_home:
                    // Navigate to settings screen
                    if (homeFragment == null) { homeFragment = new HomeFragment(); }
                    settingsManager.setBottomNavigationViewCurrentIndex(id);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, homeFragment, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    break;
                case R.id.bottom_nav_bar_calendar:
                    // Navigate to settings screen
                    if (calendarFragment == null) { calendarFragment = new CalendarFragment(); }
                    settingsManager.setBottomNavigationViewCurrentIndex(id);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, calendarFragment, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    break;
                case R.id.bottom_nav_bar_settings:
                    // Navigate to settings screen
                    if (settingsFragment == null) { settingsFragment = new SettingsFragment(); }
                    settingsManager.setBottomNavigationViewCurrentIndex(R.id.bottom_nav_bar_settings);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, settingsFragment, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
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
