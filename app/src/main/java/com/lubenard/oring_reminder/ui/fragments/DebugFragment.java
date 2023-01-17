package com.lubenard.oring_reminder.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.Utils;

public class DebugFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.debug_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.debug_menu_title);

        Button buttonSendNotif = view.findViewById(R.id.debug_send_notif);

        Switch enableLogSwitch = view.findViewById(R.id.debug_enable_logs);

        enableLogSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MainActivity.getSettingsManager().setIsLoggingEnabled(isChecked);
        });

        buttonSendNotif.setOnClickListener(view1 -> {
            SessionsAlarmsManager.sendNotificationWithQuickAnswer(getContext(), "This is a test notification",
                    "No entry is affected by this notification",
                    R.drawable.baseline_done_24, -1);
            Toast.makeText(getContext(), "Just sent a manual notif. Do not worry, no entry will be affected.", Toast.LENGTH_SHORT).show();
        });
    }
}
