package com.lubenard.oring_reminder.ui.fragments;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.UiUtils;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Date;

public class CalculatorFragment extends DialogFragment {

    private static final String TAG = "CalculatorFragment";

    private DbManager dbManager;
    private BreakSession pausesDatas;
    private RingSession session;
    private long breakId;
    private long sessionId;
    private boolean isManualEditEnabled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.calculators_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fix widget to bottom and makes the dialog take up the full width
        Window window = requireDialog().getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes());
        lp.width = 1000;
        lp.height = WRAP_CONTENT;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(lp);
        window.setGravity(Gravity.BOTTOM);

        EditText pause_beginning_date = view.findViewById(R.id.edittext_beginning_pause);
        EditText pause_beginning_time = view.findViewById(R.id.new_entry_hour_from);
        EditText pause_ending_date = view.findViewById(R.id.edittext_finish_pause);

        Log.d(TAG, "Session id is " + session.getId());

        ImageButton close_fragment_button = view.findViewById(R.id.create_new_break_cancel);
        close_fragment_button.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putBoolean("shouldUpdateBreakList", false);
            getParentFragmentManager().setFragmentResult("EditBreakFragmentResult", result);
            dismiss();
        });
    }
}
