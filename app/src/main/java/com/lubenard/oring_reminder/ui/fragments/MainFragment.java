package com.lubenard.oring_reminder.ui.fragments;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private BottomNavigationView bottom_navigation_view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottom_navigation_view = view.findViewById(R.id.bottomNavigationView);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_fragment, new HomeFragment(), null).addToBackStack(null).commit();

        bottom_navigation_view.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            Log.d(TAG, "id = " + id);
            switch (id) {
                case R.id.bottom_nav_bar_home:
                    // Navigate to settings screen
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, new HomeFragment(), null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    break;
                case R.id.bottom_nav_bar_calendar:
                    // Navigate to settings screen
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, new CalendarFragment(), null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    break;
                case R.id.bottom_nav_bar_settings:
                    // Navigate to settings screen
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, new SettingsFragment(), null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                    break;
            }
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView called");
        super.onDestroyView();
    }

}
