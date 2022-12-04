package com.lubenard.oring_reminder.ui.fragments;

import android.os.Bundle;
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
    private int bottomNavigationViewCurrentIndex;
    private HomeFragment homeFragment = null;
    private CalendarFragment calendarFragment = null;
    private SettingsFragment settingsFragment = null;

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

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_fragment, new HomeFragment(), null).addToBackStack(null).commit();

        Log.d(TAG, "onViewCreated()");

        // Avoid recreating new fragment each time, we record the current fragment
        bottomNavigationViewCurrentIndex = R.id.bottom_nav_bar_home;

        homeFragment = new HomeFragment();

        bottom_navigation_view.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            Log.d(TAG, "id = " + id + ", bottomNavigationViewCurrentIndex = " + bottomNavigationViewCurrentIndex);
            if (bottomNavigationViewCurrentIndex == id) {
                Log.d(TAG, "id = bottomNavigationViewCurrentIndex");
                return true;
            }
            switch (id) {
                case R.id.bottom_nav_bar_home:
                    // Navigate to settings screen
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, homeFragment, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    bottomNavigationViewCurrentIndex = R.id.bottom_nav_bar_home;
                    break;
                case R.id.bottom_nav_bar_calendar:
                    // Navigate to settings screen
                    if (calendarFragment == null) { calendarFragment = new CalendarFragment(); }
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, calendarFragment, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    bottomNavigationViewCurrentIndex = R.id.bottom_nav_bar_calendar;
                    break;
                case R.id.bottom_nav_bar_settings:
                    // Navigate to settings screen
                    if (settingsFragment == null) { settingsFragment = new SettingsFragment(); }
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, settingsFragment, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    bottomNavigationViewCurrentIndex = R.id.bottom_nav_bar_settings;
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
