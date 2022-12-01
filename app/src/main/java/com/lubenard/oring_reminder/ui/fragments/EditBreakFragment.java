package com.lubenard.oring_reminder.ui.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EditBreakFragment extends DialogFragment {

    private static final String TAG = "EditEntryFragment";

    private DbManager dbManager;
    private BreakSession pausesDatas;
    private RingSession sessionDatas;
    private long breakId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.custom_pause_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        breakId = bundle.getLong("entryId", -1);

        Context context = requireContext();

        EditText pause_beginning = view.findViewById(R.id.edittext_beginning_pause);
        EditText pause_ending = view.findViewById(R.id.edittext_finish_pause);

        pausesDatas = dbManager.getBreakForId(breakId);
        sessionDatas = dbManager.getEntryDetails(pausesDatas.getSessionId());

        if (pausesDatas != null) {
            pause_beginning.setText(pausesDatas.getStartDate());
            pause_ending.setText(pausesDatas.getEndDate());
        }

        Button fill_beginning = view.findViewById(R.id.prefill_beginning_pause);
        fill_beginning.setOnClickListener(v -> pause_beginning.setText(Utils.getdateFormatted(new Date())));

        Button fill_end = view.findViewById(R.id.prefill_finish_pause);
        fill_end.setOnClickListener(v -> pause_ending.setText(Utils.getdateFormatted(new Date())));

        ImageButton save_entry = view.findViewById(R.id.validate_pause);
        save_entry.setOnClickListener(v -> {
            int isRunning = 0;
            String pauseEndingText = pause_ending.getText().toString();
            String pauseBeginningText = pause_beginning.getText().toString();
            if (pauseEndingText.isEmpty() || pauseEndingText.equals("NOT SET YET")) {
                pauseEndingText = "NOT SET YET" ;
                isRunning = 1;
            }

            if (/*isThereAlreadyARunningPause &&*/ isRunning == 1) {
                Log.d(TAG, "Error: Already a running pause");
                Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
            } else if (Utils.getDateDiff(sessionDatas.getDatePut(), pauseBeginningText, TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: Start of pause < start of entry");
                Toast.makeText(context, context.getString(R.string.pause_beginning_to_small), Toast.LENGTH_SHORT).show();
            } else if (isRunning == 0 && Utils.getDateDiff(sessionDatas.getDatePut(), pauseEndingText, TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: End of pause < start of entry");
                Toast.makeText(context, context.getString(R.string.pause_ending_too_small), Toast.LENGTH_SHORT).show();
            } else if (isRunning == 0 && !sessionDatas.getIsRunning() && Utils.getDateDiff(pauseEndingText, sessionDatas.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: End of pause > end of entry");
                Toast.makeText(context, context.getString(R.string.pause_ending_too_big), Toast.LENGTH_SHORT).show();
            } else if (!sessionDatas.getIsRunning() && Utils.getDateDiff(pauseBeginningText, sessionDatas.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: Start of pause > end of entry");
                Toast.makeText(context, context.getString(R.string.pause_starting_too_big), Toast.LENGTH_SHORT).show();
            } else {
                if (pausesDatas == null) {
                    long id = dbManager.createNewPause(sessionDatas.getId(), pauseBeginningText, pauseEndingText, isRunning);
                    //createNewBreak(id, pauseBeginningText, pauseEndingText, isRunning);
                } else {
                    long id = dbManager.updatePause(pausesDatas.getId(), pauseBeginningText, pauseEndingText, isRunning);
                    long timeWorn = Utils.getDateDiff(pauseBeginningText, pauseEndingText, TimeUnit.MINUTES);
                    //pausesDatas.set(position, new RingSession((int)id, pauseEndingText, pauseBeginningText, isRunning, (int)timeWorn));
                    // Cancel the break notification if it is set as finished.
                    if (isRunning == 0) {
                        Intent intent = new Intent(getContext(), NotificationSenderBreaksBroadcastReceiver.class).putExtra("action", 1);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), (int) pausesDatas.getId(), intent, PendingIntent.FLAG_MUTABLE);
                        AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                        am.cancel(pendingIntent);
                    }
                }
                dismiss();

                //recomputeWearingTime();

                // Only recompute alarm if session is running, else cancel it.
                if (sessionDatas.getIsRunning()) {
                    if (pause_ending.getText().toString().equals("NOT SET YET")) {
                        Log.d(TAG, "Cancelling alarm for entry: " + sessionDatas.getId());
                        SessionsAlarmsManager.cancelAlarm(context, sessionDatas.getId());
                    } else {
                        //TODO: uncomment this method
                        //Calendar calendar = Calendar.getInstance();
                        //calendar.setTime(Utils.getdateParsed(sessionDatas.getDatePut()));
                        //calendar.add(Calendar.MINUTE, newAlarmDate);
                        //Log.d(TAG, "Setting alarm for entry: " + sessionDatas.getId() + " At: " + Utils.getdateFormatted(calendar.getTime()));
                        // Cancel break alarm is session is set as finished
                        /*if (new SettingsManager(context).getShouldSendNotifWhenBreakTooLong()) {
                            Intent intent = new Intent(getContext(), NotificationSenderBreaksBroadcastReceiver.class)
                                    .putExtra("action", 1);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), (int) sessionDatas.getId(), intent, 0);
                            AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                            am.cancel(pendingIntent);
                        }
                        SessionsAlarmsManager.setAlarm(context, Utils.getdateFormatted(calendar.getTime()), sessionDatas.getId(), true);
                        */
                    }
                }
                if (isRunning == 1)
                    SessionsAlarmsManager.setBreakAlarm(context, pause_beginning.getText().toString(),  sessionDatas.getId());
                //updatePauseList();
                EditEntryFragment.updateWidget(getContext());
            }
        });
    }
}
