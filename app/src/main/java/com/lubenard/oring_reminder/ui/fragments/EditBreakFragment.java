package com.lubenard.oring_reminder.ui.fragments;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import android.text.method.KeyListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.utils.UiUtils;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EditBreakFragment extends DialogFragment {

    private static final String TAG = "EditBreakFragment";

    private DbManager dbManager;
    private BreakSession pausesDatas;
    private RingSession sessionDatas;
    private long breakId;
    private long sessionId;
    private boolean isManualEditEnabled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.edit_break_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        breakId = bundle.getLong("breakId", -1);
        sessionId = bundle.getLong("sessionId", -1);

        Context context = requireContext();

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
        EditText pause_ending_time = view.findViewById(R.id.new_entry_hour_to);

        dbManager = MainActivity.getDbManager();

        sessionDatas = dbManager.getEntryDetails(sessionId);

        Log.d(TAG, "Session id is " + sessionDatas.getId());

        if (breakId != -1) {
            pausesDatas = dbManager.getBreakForId(breakId);
            if (pausesDatas != null) {
                String[] startDateSplitted = pausesDatas.getStartDate().split(" ");
                String[] endDateSplitted = pausesDatas.getEndDate().split(" ");

                pause_beginning_date.setText(startDateSplitted[0]);
                pause_beginning_time.setText(startDateSplitted[1]);
                pause_ending_date.setText(endDateSplitted[0]);
                pause_ending_date.setText(endDateSplitted[1]);
            }
        }

        UiUtils.disableEditText(pause_beginning_date);
        UiUtils.disableEditText(pause_ending_date);
        UiUtils.disableEditText(pause_beginning_time);
        UiUtils.disableEditText(pause_ending_time);

        pause_beginning_date.setOnClickListener(v -> UiUtils.openCalendarPicker(context, pause_beginning_date));
        pause_beginning_time.setOnClickListener(v -> UiUtils.openTimePicker(context, pause_beginning_time));
        pause_ending_date.setOnClickListener(v -> UiUtils.openCalendarPicker(context, pause_ending_date));
        pause_ending_time.setOnClickListener(v -> UiUtils.openTimePicker(context, pause_ending_time));

        Button fill_beginning = view.findViewById(R.id.prefill_beginning_pause);
        fill_beginning.setOnClickListener(v -> {
            String[] currentDate = DateUtils.getdateFormatted(new Date()).split(" ");
            pause_beginning_date.setText(currentDate[0]);
            pause_beginning_time.setText(currentDate[1]);
        });

        Button fill_end = view.findViewById(R.id.prefill_finish_pause);
        fill_end.setOnClickListener(v -> {
            String[] currentDate = DateUtils.getdateFormatted(new Date()).split(" ");
            pause_ending_date.setText(currentDate[0]);
            pause_ending_time.setText(currentDate[1]);
        });

        ImageButton close_fragment_button = view.findViewById(R.id.create_new_break_cancel);
        close_fragment_button.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putBoolean("shouldUpdateBreakList", false);
            getParentFragmentManager().setFragmentResult("EditBreakFragmentResult", result);
            dismiss();
        });

        ImageButton manualEditButton = view.findViewById(R.id.manual_edit_pause);
        manualEditButton.setOnClickListener(v -> {
            if (isManualEditEnabled) {
                Log.d(TAG, "Disabling editTexts");
                UiUtils.disableEditText(pause_beginning_date);
                UiUtils.disableEditText(pause_ending_date);
                UiUtils.disableEditText(pause_beginning_time);
                UiUtils.disableEditText(pause_ending_time);
                Toast.makeText(requireContext(), R.string.manual_mode_disabled, Toast.LENGTH_SHORT).show();

                isManualEditEnabled = false;
            } else {
                Log.d(TAG, "Enabling editTexts");
                UiUtils.enableEditText(pause_beginning_date);
                UiUtils.enableEditText(pause_ending_date);
                UiUtils.enableEditText(pause_beginning_time);
                UiUtils.enableEditText(pause_ending_time);
                Toast.makeText(requireContext(), R.string.manual_mode_enabled, Toast.LENGTH_SHORT).show();
                isManualEditEnabled = true;
            }
        });

        ImageButton save_entry = view.findViewById(R.id.validate_pause);
        save_entry.setOnClickListener(v -> {
            int isRunning = 0;
            String pauseEndingDateText = pause_ending_date.getText().toString();
            String pauseEndingTimeText = pause_ending_time.getText().toString();
            String pauseBeginningDateText = pause_beginning_date.getText().toString();
            String pauseBeginningTimeText = pause_beginning_time.getText().toString();
            if ((pauseEndingDateText.isEmpty() || pauseEndingDateText.equals("NOT SET YET"))
                    && (pauseEndingTimeText.isEmpty() || pauseEndingTimeText.equals("NOT SET YET"))) {
                isRunning = 1;
            }

            String pauseBeginningText = String.format("%s %s", pauseBeginningDateText, pauseBeginningTimeText);
            String pauseEndingText = String.format("%s %s", pauseEndingDateText, pauseEndingTimeText);

            if (isRunning == 1) {
                pauseEndingText = "NOT SET YET";
            }

            BreakSession newBreakSession;

            if (pausesDatas != null) {
                newBreakSession = new BreakSession(pausesDatas.getId(), pauseBeginningText, pauseEndingText, isRunning, 0, sessionDatas.getId());
            } else {
                newBreakSession = new BreakSession(-1, pauseBeginningText, pauseEndingText, isRunning, 0, sessionDatas.getId());
            }

            Log.d(TAG, "new BreakSession has " + newBreakSession.getStartDate() + " as starting date");

            if (SessionsManager.startBreak2(context, sessionDatas, newBreakSession, pausesDatas == null)) {
                // break inserted successfully
                Bundle result = new Bundle();
                result.putBoolean("shouldUpdateBreakList", true);
                getParentFragmentManager().setFragmentResult("EditBreakFragmentResult", result);
                dismiss();
            }

            /*
                // Only recompute alarm if session is running, else cancel it.
                if (sessionDatas.getIsRunning()) {
                    if (pause_ending_date.getText().toString().equals("NOT SET YET")) {
                        Log.d(TAG, "Cancelling alarm for entry: " + sessionDatas.getId());
                        SessionsAlarmsManager.cancelAlarm(context, sessionDatas.getId());
                    } else {
                        //TODO: uncomment this method
                        //Calendar calendar = Calendar.getInstance();
                        //calendar.setTime(DateUtils.getdateParsed(sessionDatas.getDatePut()));
                        //calendar.add(Calendar.MINUTE, newAlarmDate);
                        //Log.d(TAG, "Setting alarm for entry: " + sessionDatas.getId() + " At: " + DateUtils.getdateFormatted(calendar.getTime()));
                        // Cancel break alarm is session is set as finished
                        if (new SettingsManager(context).getShouldSendNotifWhenBreakTooLong()) {
                            Intent intent = new Intent(getContext(), NotificationSenderBreaksBroadcastReceiver.class)
                                    .putExtra("action", 1);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), (int) sessionDatas.getId(), intent, 0);
                            AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                            am.cancel(pendingIntent);
                        }
                        //SessionsAlarmsManager.setAlarm(context, DateUtils.getdateFormatted(calendar.getTime()), sessionDatas.getId(), true);
                    }
                }
                if (isRunning == 1)
                    SessionsAlarmsManager.setBreakAlarm(context, pause_beginning_date.getText().toString(),  sessionDatas.getId());
                //updatePauseList();
                EditEntryFragment.updateWidget(getContext());
            }*/
        });
    }
}
