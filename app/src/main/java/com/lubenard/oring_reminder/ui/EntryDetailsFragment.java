package com.lubenard.oring_reminder.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.NotificationSenderBroadcastReceiver;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.CustomListPausesAdapter;
import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION.SDK_INT;

public class EntryDetailsFragment extends Fragment {

    public static final String TAG = "EntryDetailsFragment";

    private long entryId = -1;
    private DbManager dbManager;
    private int weared_time;
    private View view;
    private Context context;
    private FragmentManager fragmentManager;
    private ListView listView;
    private CustomListPausesAdapter adapter;
    private ArrayList<RingModel> dataModels;
    private int newAlarmDate;
    private RingModel entryDetails;
    private TextView ableToGetItOff;
    private TextView whenGetItOff;
    private TextView timeWeared;
    private Button stopSessionButton;
    private boolean isThereAlreadyARunningPause = false;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.entry_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = getContext();
        fragmentManager = getActivity().getSupportFragmentManager();
        this.view = view;

        dbManager = new DbManager(context);
        dataModels = new ArrayList<>();

        Bundle bundle = this.getArguments();
        entryId = bundle.getLong("entryId", -1);

        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        weared_time = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));

        listView = view.findViewById(R.id.listview_pauses);

        stopSessionButton = view.findViewById(R.id.button_finish_session);

        stopSessionButton.setOnClickListener(view13 -> {
            dbManager.endSession(entryId);
            updateAllFragmentDatas();
        });

        ImageButton pauseButton = view.findViewById(R.id.new_pause_button);
        pauseButton.setOnClickListener(view1 -> showPauseAlertDialog(null));
        pauseButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (entryDetails.getIsRunning() == 1) {
                    dbManager.createNewPause(entryId, Utils.getdateFormatted(new Date()), "NOT SET YET", 1);
                    // Cancel alarm until breaks are set as finished.
                    // Only then set a new alarm date
                    Log.d(TAG, "Cancelling alarm for entry: " + entryId);
                    EditEntryFragment.cancelAlarm(context, entryId);
                    updatePauseList();
                } else
                    Toast.makeText(context, R.string.no_pause_session_is_not_running, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        listView.setOnItemClickListener((adapterView, view12, i, l) -> showPauseAlertDialog(dataModels.get(i)));

        listView.setOnItemLongClickListener((arg0, arg1, pos, id) -> {
            new AlertDialog.Builder(context).setTitle(R.string.alertdialog_delete_entry)
                    .setMessage(R.string.alertdialog_delete_contact_body)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        RingModel object = (RingModel) arg0.getItemAtPosition(pos);
                        Log.d(TAG, "delete pause with id: " + object.getId());
                        dbManager.deletePauseEntry(object.getId());
                        updatePauseList();
                        recomputeWearingTime();
                        if (entryDetails.getIsRunning() == 1) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(Utils.getdateParsed(entryDetails.getDatePut()));
                            calendar.add(Calendar.MINUTE, newAlarmDate);
                            Log.d(TAG, "Setting alarm for entry: " + entryId + " At: " + Utils.getdateFormatted(calendar.getTime()));
                            EditEntryFragment.setAlarm(context, Utils.getdateFormatted(calendar.getTime()), entryId, true);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert).show();
            return true;
        });
    }

    /**
     * Show user pause alert dialog
     * Also compute if pause it in the session interval
     * @param dataModel If the pause already exist, give it datas to load
     */
    private void showPauseAlertDialog(RingModel dataModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ViewGroup viewGroup = view.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.custom_pause_dialog, viewGroup, false);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        EditText pause_beginning = dialogView.findViewById(R.id.edittext_beginning_pause);
        EditText pause_ending = dialogView.findViewById(R.id.edittext_finish_pause);

        if (dataModel != null) {
            pause_beginning.setText(dataModel.getDateRemoved());
            pause_ending.setText(dataModel.getDatePut());
        }

        Button fill_beginning = dialogView.findViewById(R.id.prefill_beginning_pause);
        fill_beginning.setOnClickListener(view -> pause_beginning.setText(Utils.getdateFormatted(new Date())));

        Button fill_end = dialogView.findViewById(R.id.prefill_finish_pause);
        fill_end.setOnClickListener(view -> pause_ending.setText(Utils.getdateFormatted(new Date())));

        Button save_entry = dialogView.findViewById(R.id.validate_pause);
        save_entry.setOnClickListener(view -> {
            int isRunning = 0;
            if (pause_ending.getText().toString().isEmpty()) {
                pause_ending.setText("NOT SET YET");
                isRunning = 1;
            }

            if (isThereAlreadyARunningPause && isRunning == 1){
                Log.d(TAG, "Error: Already a running pause");
                Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
            } else if (Utils.getDateDiff(entryDetails.getDatePut(), pause_beginning.getText().toString(), TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: Start of pause < start of entry");
                Toast.makeText(context, context.getString(R.string.pause_beginning_to_small), Toast.LENGTH_SHORT).show();
            } else if (isRunning == 0 && Utils.getDateDiff(entryDetails.getDatePut(), pause_ending.getText().toString(), TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: End of pause < start of entry");
                Toast.makeText(context, context.getString(R.string.pause_ending_too_small), Toast.LENGTH_SHORT).show();
            } else if (isRunning == 0 && entryDetails.getIsRunning() == 0 && Utils.getDateDiff(pause_ending.getText().toString(), entryDetails.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: End of pause > end of entry");
                Toast.makeText(context, context.getString(R.string.pause_ending_too_big), Toast.LENGTH_SHORT).show();
            } else if (entryDetails.getIsRunning() == 0 && Utils.getDateDiff(pause_beginning.getText().toString(), entryDetails.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: Start of pause > end of entry");
                Toast.makeText(context, context.getString(R.string.pause_starting_too_big), Toast.LENGTH_SHORT).show();
            } else {
                if (dataModel == null)
                    dbManager.createNewPause(entryId, pause_beginning.getText().toString(), pause_ending.getText().toString(), isRunning);
                else {
                    dbManager.updatePause(dataModel.getId(), pause_beginning.getText().toString(), pause_ending.getText().toString(), isRunning);
                    // Cancel the break notification if it is set as finished.
                    if (isRunning == 0) {
                        Intent intent = new Intent(getContext(), NotificationSenderBreaksBroadcastReceiver.class)
                                .putExtra("action", 1);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), (int) ((dataModel != null) ? dataModel.getId() : entryId), intent, 0);
                        AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                        am.cancel(pendingIntent);
                    }
                }
                alertDialog.dismiss();
                recomputeWearingTime();

                // Only recompute alarm if session is running, else cancel it.
                if (entryDetails.getIsRunning() == 1) {
                    if (pause_ending.getText().toString().equals("NOT SET YET")) {
                        Log.d(TAG, "Cancelling alarm for entry: " + entryId);
                        EditEntryFragment.cancelAlarm(context, entryId);
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(Utils.getdateParsed(entryDetails.getDatePut()));
                        calendar.add(Calendar.MINUTE, newAlarmDate);
                        Log.d(TAG, "Setting alarm for entry: " + entryId + " At: " + Utils.getdateFormatted(calendar.getTime()));
                        EditEntryFragment.setAlarm(context, Utils.getdateFormatted(calendar.getTime()), entryId, true);
                    }
                }
                // Add alarm if break is too long (only if break is running)
                if (sharedPreferences.getBoolean("myring_prevent_me_when_pause_too_long", false) && isRunning == 1) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(Utils.getdateParsed(pause_beginning.getText().toString()));
                    calendar.add(Calendar.MINUTE, sharedPreferences.getInt("myring_prevent_me_when_pause_too_long_date", 0));
                    Intent intent = new Intent(getContext(), NotificationSenderBreaksBroadcastReceiver.class)
                            .putExtra("action", 1);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), (int) ((dataModel != null) ? dataModel.getId() : entryId), intent, 0);
                    AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);

                    if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M)
                        am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    else if (SDK_INT >= Build.VERSION_CODES.M)
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
                updatePauseList();
            }
        });
        alertDialog.show();
    }

    /**
     * Compute when user an get it off according to breaks.
     * If the user made a 1h30 break, then he should wear it 1h30 more
     */
    private void recomputeWearingTime() {
        long oldTimeWeared;
        // If session is running,
        // OldTimeWeared is the time in minute between the starting of the entry and the current Date
        // Or, oldTimeWeared is the time between the start of the entry and it's pause
        if (entryDetails.getIsRunning() == 1)
            oldTimeWeared = Utils.getDateDiff(entryDetails.getDatePut(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
        else
            oldTimeWeared = Utils.getDateDiff(entryDetails.getDatePut(), entryDetails.getDateRemoved(), TimeUnit.MINUTES);
        long totalTimePause = 0;
        int newComputedTime;

        ArrayList<RingModel> pausesDatas = dbManager.getAllPausesForId(entryId, true);

        isThereAlreadyARunningPause = false;

        for (int i = 0; i < pausesDatas.size(); i++) {
            if (pausesDatas.get(i).getIsRunning() == 0) {
                totalTimePause += pausesDatas.get(i).getTimeWeared();
            } else {
                long timeToRemove = Utils.getDateDiff(pausesDatas.get(i).getDateRemoved(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                totalTimePause += timeToRemove;
                isThereAlreadyARunningPause = true;
            }
        }

        // Avoid having more time pause than weared time
        if (totalTimePause > oldTimeWeared)
            totalTimePause = oldTimeWeared;

        newComputedTime = (int) (oldTimeWeared - totalTimePause);
        Log.d(TAG, "Compute newWearingTime = " + oldTimeWeared + " - " + totalTimePause + " = " + newComputedTime);
        timeWeared.setText(String.format("%dh%02dm", newComputedTime / 60, newComputedTime % 60));

        // Time is computed as:
        // Date of put + number_of_hour_defined_in settings + total_time_in_pause
        newAlarmDate = (int) (weared_time * 60 + totalTimePause);
        Log.d(TAG, "New alarm date = " + newAlarmDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utils.getdateParsed(entryDetails.getDatePut()));
        calendar.add(Calendar.MINUTE, newAlarmDate);
        updateAbleToGetItOffUI(calendar);
    }

    /**
     * Compute the relative time when user can get protection of
     * @param calendar the time the user can remove the protection
     */
    private void updateAbleToGetItOffUI(Calendar calendar) {
        int texteRessourceWhenGetItOff;

        long timeBeforeRemove = Utils.getDateDiff(Utils.getdateFormatted(new Date()), Utils.getdateFormatted(calendar.getTime()), TimeUnit.MINUTES);
        Log.d(TAG, "timeBeforeRemove = " + timeBeforeRemove);

        ableToGetItOff.setText(getString(R.string._message_able_to_get_it_off) + Utils.getdateFormatted(calendar.getTime()));
        if (timeBeforeRemove >= 0)
            texteRessourceWhenGetItOff = R.string.in_about_entry_details;
        else {
            texteRessourceWhenGetItOff = R.string.when_get_it_off_negative;
            timeBeforeRemove *= -1;
        }

        whenGetItOff.setText(String.format(getString(texteRessourceWhenGetItOff), timeBeforeRemove / 60, timeBeforeRemove % 60));
    }

    /**
     * Update the listView by fetching all elements from the db
     */
    private void updatePauseList() {
       dataModels.clear();
       ArrayList<RingModel> pausesDatas = dbManager.getAllPausesForId(entryId, true);

       dataModels.addAll(pausesDatas);
       adapter = new CustomListPausesAdapter(dataModels, getContext());
       listView.setAdapter(adapter);
        Utils.getListViewSize(listView);
    }

    private void updateAllFragmentDatas() {
        if (entryId > 0) {
            // Load datas from the db and put them at the right place
            entryDetails = dbManager.getEntryDetails(entryId);
            TextView put = view.findViewById(R.id.details_entry_put);
            TextView removed = view.findViewById(R.id.details_entry_removed);
            timeWeared = view.findViewById(R.id.details_entry_time_weared);
            TextView isRunning = view.findViewById(R.id.details_entry_isRunning);
            ableToGetItOff = view.findViewById(R.id.details_entry_able_to_get_it_off);
            whenGetItOff = view.findViewById(R.id.details_entry_when_get_it_off);

            // Choose color if the timeWeared is enough or not
            // Depending of the timeWeared set in the settings
            if (entryDetails.getIsRunning() == 0 && entryDetails.getTimeWeared() / 60 >= weared_time)
                timeWeared.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            else if (entryDetails.getIsRunning() == 0 && entryDetails.getTimeWeared() / 60 < weared_time)
                timeWeared.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            else
                timeWeared.setTextColor(getResources().getColor(R.color.yellow));

            put.setText(entryDetails.getDatePut());
            removed.setText(entryDetails.getDateRemoved());

            // Check if the session is finished and display the corresponding text
            // Either 'Not set yet', saying the session is not over
            // Or the endSession date
            if (entryDetails.getIsRunning() == 1) {
                long timeBeforeRemove = Utils.getDateDiff(entryDetails.getDatePut(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                timeWeared.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
            } else {
                int time_spent_wearing = entryDetails.getTimeWeared();
                if (time_spent_wearing < 60)
                    timeWeared.setText(entryDetails.getTimeWeared() + getString(R.string.minute_with_M_uppercase));
                else
                    timeWeared.setText(String.format("%dh%02dm", time_spent_wearing / 60, time_spent_wearing % 60));
            }

            // Display the datas relative to the session
            if (entryDetails.getIsRunning() == 1) {
                isRunning.setTextColor(getResources().getColor(R.color.yellow));
                isRunning.setText(R.string.session_is_running);

                stopSessionButton.setVisibility(View.VISIBLE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Utils.getdateParsed(entryDetails.getDatePut()));
                calendar.add(Calendar.HOUR_OF_DAY, weared_time);
                updateAbleToGetItOffUI(calendar);
            } else {
                // If the session is finished, no need to show the ableToGetItOff textView.
                // This textview is only used to warn user when he will be able to get it off
                isRunning.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                isRunning.setText(R.string.session_finished);
                ableToGetItOff.setVisibility(View.INVISIBLE);
                whenGetItOff.setVisibility(View.INVISIBLE);
                stopSessionButton.setVisibility(View.GONE);
            }
            recomputeWearingTime();
            updatePauseList();
        } else {
            // Trigger an error if the entryId is wrong, then go back to main list
            Toast.makeText(context, context.getString(R.string.error_bad_id_entry_details) + entryId, Toast.LENGTH_SHORT);
            Log.e(TAG, "Error: Wrong Id: " + entryId);
            fragmentManager.popBackStackImmediate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllFragmentDatas();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_entry_details, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit_entry:
                EditEntryFragment fragment = new EditEntryFragment(getContext());
                Bundle bundle2 = new Bundle();
                bundle2.putLong("entryId", entryId);
                fragment.setArguments(bundle2);
                fragmentManager.beginTransaction()
                        .replace(android.R.id.content, fragment, null)
                        .addToBackStack(null).commit();
                return true;
            case R.id.action_delete_entry:
                // Warn user then delete entry in the db
                new AlertDialog.Builder(context).setTitle(R.string.alertdialog_delete_entry)
                        .setMessage(R.string.alertdialog_delete_contact_body)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            dbManager.deleteEntry(entryId);
                            fragmentManager.popBackStackImmediate();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
                return true;
            default:
                return false;
        }
    }
}
