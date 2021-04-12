package com.lubenard.oring_reminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.utils.Utils;

public class DebugFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.debug_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.debug_menu_title);

        Button buttonSendNotif = view.findViewById(R.id.debug_send_notif);

        buttonSendNotif.setOnClickListener(view1 -> {
            Utils.sendNotificationWithQuickAnswer(getContext(), "This is a test notification",
                    "No entry is affected by this notification",
                    R.drawable.baseline_done_24, -1);
            Toast.makeText(getContext(), "Just sent a manual notif. Do not worry, no entry will be affected.", Toast.LENGTH_SHORT).show();
        });
    }
}
