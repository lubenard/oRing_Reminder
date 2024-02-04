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

import androidx.fragment.app.DialogFragment;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.custom_components.Session;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.UiUtils;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Date;

public class EditBreakFragment extends DialogFragment {

    private static final String TAG = "EditBreakFragment";

    private DbManager dbManager;
    private BreakSession pausesDatas;
    private RingSession session;
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

        session = dbManager.getEntryDetails(sessionId);

        Log.d(TAG, "Session id is " + session.getId());

        if (breakId != -1) {
            pausesDatas = dbManager.getBreakForId(breakId);
            if (pausesDatas != null) {
                String[] startDateSplitted = pausesDatas.getStartDate().split(" ");
                String[] endDateSplitted = pausesDatas.getEndDate().split(" ");

                Log.d(TAG, "EndDate is " + pausesDatas.getEndDate());

                pause_beginning_date.setText(startDateSplitted[0]);
                pause_beginning_time.setText(startDateSplitted[1]);
                if (!(pausesDatas.getStatus() == Session.SessionStatus.RUNNING)) {
                    pause_ending_date.setText(endDateSplitted[0]);
                    pause_ending_time.setText(endDateSplitted[1]);
                }
            }
        }

        UiUtils.disableEditText(pause_beginning_date);
        UiUtils.disableEditText(pause_ending_date);
        UiUtils.disableEditText(pause_beginning_time);
        UiUtils.disableEditText(pause_ending_time);

        pause_beginning_date.setOnClickListener(v -> UiUtils.openCalendarPicker(context, pause_beginning_date, true));
        pause_beginning_time.setOnClickListener(v -> UiUtils.openTimePicker(context, pause_beginning_time, true));
        pause_ending_date.setOnClickListener(v -> UiUtils.openCalendarPicker(context, pause_ending_date, true));
        pause_ending_time.setOnClickListener(v -> UiUtils.openTimePicker(context, pause_ending_time, true));

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
                Utils.hideKbd(context, getView().getRootView().getWindowToken());
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

            BreakSession newBreakSession = new BreakSession(
                    (pausesDatas != null) ? pausesDatas.getId() : -1,
                    pauseBeginningText,
                    pauseEndingText,
                    isRunning,
                    0,
                    session.getId()
            );

            Log.d(TAG, "new BreakSession has " + newBreakSession.getStartDate() + " as starting date");

            if (SessionsManager.startBreak2(context, session, newBreakSession, pausesDatas == null)) {
                // Break inserted successfully

                // Only recompute alarm if session is running, else cancel it.
                if (session.getStatus() == Session.SessionStatus.RUNNING) {
                    if (newBreakSession.getStatus() == Session.SessionStatus.RUNNING) {
                        Log.d(TAG, "Cancelling alarm for entry: " + session.getId());
                        SessionsAlarmsManager.cancelAlarm(context, session.getId());
                    } else {
                        //TODO: uncomment this method
                        //Calendar calendar = Calendar.getInstance();
                        //calendar.setTime(DateUtils.getdateParsed(sessionDatas.getDatePut()));
                        //calendar.add(Calendar.MINUTE, newAlarmDate);
                        //Log.d(TAG, "Setting alarm for entry: " + sessionDatas.getId() + " At: " + DateUtils.getdateFormatted(calendar.getTime()));
                        // Cancel break alarm is session is set as finished
                        if (MainActivity.getSettingsManager().getShouldSendNotifWhenBreakTooLong()) {
                            Intent intent = new Intent(getContext(), NotificationSenderBreaksBroadcastReceiver.class)
                                    .putExtra("action", 1);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), (int) session.getId(), intent, PendingIntent.FLAG_MUTABLE);
                            AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                            am.cancel(pendingIntent);
                        }
                        //SessionsAlarmsManager.setAlarm(context, , true);
                    }
                }
                if (isRunning == 1)
                    SessionsAlarmsManager.setBreakAlarm(context, pause_beginning_date.getText().toString(), session.getId());
                Utils.updateWidget(getContext());

                Bundle result = new Bundle();
                result.putBoolean("shouldUpdateBreakList", true);
                getParentFragmentManager().setFragmentResult("EditBreakFragmentResult", result);
                dismiss();
            }
        });
    }
}
