package com.lubenard.oring_reminder.ui.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EntryDetailsFragment extends Fragment {

    private static final String TAG = "EntryDetailsFragment";

    private long entryId = -1;
    private DbManager dbManager;
    private int weared_time;
    private View view;
    private Context context;
    private FragmentManager fragmentManager;
    private ArrayList<BreakSession> pausesDatas;
    private int newAlarmDate;
    private RingSession entryDetails;
    private TextView whenGetItOff;
    private TextView textview_progress;
    private TextView textview_total_time;
    private TextView textview_percentage_progression;
    private FloatingActionButton stopSessionButton;
    private boolean isThereAlreadyARunningPause = false;
    private SettingsManager settingsManager;
    private CircularProgressIndicator progressBar;
    LinearLayout break_layout;

    private LinearLayout end_session;
    private LinearLayout estimated_end;

    private TextView put;
    private TextView removed;
    private TextView isRunning;
    private TextView estimated_end_date;
    private TextView total_breaks;
    private TextView total_time_breaks;


    private final MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_entry_details, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            int id = menuItem.getItemId();
            switch (id) {
                case R.id.action_edit_entry:
                    EditEntryFragment fragment = new EditEntryFragment();
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
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.entry_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = getContext();
        fragmentManager = getActivity().getSupportFragmentManager();
        this.view = view;

        Bundle bundle = this.getArguments();
        entryId = bundle.getLong("entryId", -1);

        dbManager = MainActivity.getDbManager();

        pausesDatas = dbManager.getAllPausesForId(entryId, true);

        Log.d(TAG, "pause datas is size " + pausesDatas.size());

        break_layout = view.findViewById(R.id.listview_pauses);

        settingsManager = MainActivity.getSettingsManager();

        weared_time =  settingsManager.getWearingTimeInt();

        stopSessionButton = view.findViewById(R.id.button_finish_session);

        end_session = view.findViewById(R.id.details_entry_end);
        estimated_end = view.findViewById(R.id.details_entry_estimated);

        put = view.findViewById(R.id.details_entry_put);
        removed = view.findViewById(R.id.details_entry_removed);
        estimated_end_date = view.findViewById(R.id.details_entry_estimated_removed);
        total_breaks = view.findViewById(R.id.details_entry_break_number);
        total_time_breaks = view.findViewById(R.id.details_entry_total_break_time);

        //isRunning = view.findViewById(R.id.details_entry_isRunning);
        textview_progress = view.findViewById(R.id.text_view_progress);
        textview_percentage_progression = view.findViewById(R.id.details_percentage_completion);
        textview_total_time = view.findViewById(R.id.text_view_total_progress);
        progressBar = view.findViewById(R.id.details_progress_bar);
        whenGetItOff = view.findViewById(R.id.details_entry_when_get_it_off);

        stopSessionButton.setOnClickListener(view13 -> {
            dbManager.endSession(entryId);
            updateAllFragmentDatas(false);
            EditEntryFragment.updateWidget(context);
        });

        ImageButton pauseButton = view.findViewById(R.id.new_pause_button);
        pauseButton.setOnClickListener(view1 -> showPauseEditBreakFragment(null));
        pauseButton.setOnLongClickListener(view12 -> {
            if (isThereAlreadyARunningPause) {
                Log.d(TAG, "Error: Already a running pause");
                Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
            } else if (entryDetails.getIsRunning()) {
                String date = Utils.getdateFormatted(new Date());
                long id = dbManager.createNewPause(entryId, date, "NOT SET YET", 1);
                // Cancel alarm until breaks are set as finished.
                // Only then set a new alarm date
                Log.d(TAG, "Cancelling alarm for entry: " + entryId);
                SessionsAlarmsManager.cancelAlarm(context, entryId);
                SessionsAlarmsManager.setBreakAlarm(context ,Utils.getdateFormatted(new Date()), entryId);
                createNewBreak(id, date, "NOT SET YET", 1);
                updatePauseList();
                EditEntryFragment.updateWidget(getContext());
            } else
                Toast.makeText(context, R.string.no_pause_session_is_not_running, Toast.LENGTH_SHORT).show();
            return true;
        });
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.CREATED);
    }

    // TODO: See if this method is still useful after huge code refactor
    private void createNewBreak(long id, String startDate, String endDate, int isRunning) {
        pausesDatas.add(0, new BreakSession((int)id, endDate, startDate, isRunning, 0, 0));
    }

    /**
     * Show user pause Edit break fragment
     * Also compute if pause it in the session interval
     * @param dataModel If the pause already exist, give it datas to load
     */
    private void showPauseEditBreakFragment(BreakSession dataModel) {
        EditBreakFragment fragment = new EditBreakFragment();
        Bundle bundle = new Bundle();
        long breakId;
        if (dataModel != null)
            breakId = dataModel.getId();
        else
            breakId = -1;
        bundle.putLong("breakId", breakId);
        fragment.setArguments(bundle);
        fragment.show(getActivity().getSupportFragmentManager(), null);
    }

    private View.OnClickListener clickInLinearLayout() {
        return v -> {
            int position = Integer.parseInt(v.getTag().toString());
            Log.d(TAG, "Clicked item at position: " + position);

            showPauseEditBreakFragment(pausesDatas.get(position));
        };
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
        if (entryDetails.getIsRunning())
            oldTimeWeared = Utils.getDateDiff(entryDetails.getDatePut(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
        else
            oldTimeWeared = Utils.getDateDiff(entryDetails.getDatePut(), entryDetails.getDateRemoved(), TimeUnit.MINUTES);
        long totalTimePause = 0;
        int newComputedTime;

        isThereAlreadyARunningPause = false;

        for (int i = 0; i < pausesDatas.size(); i++) {
            if (!pausesDatas.get(i).getIsRunning()) {
                totalTimePause += pausesDatas.get(i).getTimeRemoved();
            } else {
                long timeToRemove = Utils.getDateDiff(pausesDatas.get(i).getStartDate(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                totalTimePause += timeToRemove;
                isThereAlreadyARunningPause = true;
            }
        }

        // Avoid having more time pause than weared time
        if (totalTimePause > oldTimeWeared)
            totalTimePause = oldTimeWeared;

        newComputedTime = (int) (oldTimeWeared - totalTimePause);
        Log.d(TAG, "Compute newWearingTime = " + oldTimeWeared + " - " + totalTimePause + " = " + newComputedTime);
        textview_progress.setText(String.format("%dh%02dm", newComputedTime / 60, newComputedTime % 60));

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

        long timeBeforeRemove = Utils.getDateDiff(new Date(), calendar.getTime(), TimeUnit.MINUTES);
        Log.d(TAG, "timeBeforeRemove = " + timeBeforeRemove);

        String[] ableToGetItOffStringDate = Utils.getdateFormatted(calendar.getTime()).split(" ");
        estimated_end_date.setText(Utils.convertDateIntoReadable(ableToGetItOffStringDate[0], false) + "\n" + ableToGetItOffStringDate[1]);
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
        break_layout.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i != pausesDatas.size(); i++) {
            Log.d(TAG, "Inflating breaks");
            View view = inflater.inflate(R.layout.details_break_one_elem, break_layout, false);
            view.setTag(Integer.toString(i));

            String[] dateRemoved = pausesDatas.get(i).getStartDate().split(" ");

            TextView wornForTextView = view.findViewById(R.id.worn_for_history);
            wornForTextView.setText(R.string.removed_during);

            TextView textView_date = view.findViewById(R.id.main_history_date);
            textView_date.setText(Utils.convertDateIntoReadable(dateRemoved[0], false));

            TextView textView_hour_from = view.findViewById(R.id.custom_view_date_weared_from);
            textView_hour_from.setText(dateRemoved[1]);

            TextView textView_hour_to = view.findViewById(R.id.custom_view_date_weared_to);

            TextView textView_worn_for = view.findViewById(R.id.custom_view_date_time_weared);

            if (!pausesDatas.get(i).getIsRunning()) {
                String[] datePut = pausesDatas.get(i).getEndDate().split(" ");
                textView_hour_to.setText(datePut[1]);
                if (!dateRemoved[0].equals(datePut[0]))
                    textView_date.setText(Utils.convertDateIntoReadable(dateRemoved[0], false) + " -> " + Utils.convertDateIntoReadable(datePut[0], false));
                textView_worn_for.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                textView_worn_for.setText(Utils.convertTimeWeared(pausesDatas.get(i).getTimeRemoved()));
            } else {
                long timeworn = Utils.getDateDiff(pausesDatas.get(i).getStartDate(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                textView_worn_for.setTextColor(getContext().getResources().getColor(R.color.yellow));
                textView_worn_for.setText(String.format("%dh%02dm", timeworn / 60, timeworn % 60));
                textView_hour_to.setText("Not set yet");
            }

            view.setOnClickListener(clickInLinearLayout());
            view.setOnLongClickListener(v -> {
                new AlertDialog.Builder(context).setTitle(R.string.alertdialog_delete_entry)
                        .setMessage(R.string.alertdialog_delete_contact_body)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            int position = Integer.parseInt(v.getTag().toString());
                            BreakSession object = (BreakSession) pausesDatas.get(position);
                            Log.d(TAG, "pauseDatas size ?? " + pausesDatas.size());
                            pausesDatas.remove(object);
                            Log.d(TAG, "pauseDatas size " + pausesDatas.size());
                            Log.d(TAG, "delete pause with id: " + object.getId() + " and index " + position);
                            dbManager.deletePauseEntry(object.getId());
                            updatePauseList();
                            recomputeWearingTime();
                            if (entryDetails.getIsRunning()) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(Utils.getdateParsed(entryDetails.getDatePut()));
                                calendar.add(Calendar.MINUTE, newAlarmDate);
                                Log.d(TAG, "Setting alarm for entry: " + entryId + " At: " + Utils.getdateFormatted(calendar.getTime()));
                                SessionsAlarmsManager.setAlarm(context, Utils.getdateFormatted(calendar.getTime()), entryId, true);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
                return true;
            });
            break_layout.addView(view);
        }
    }

    /**
     * Update all displayed infos on EntryDetailsFragment with latest datas from db
     */
    private void updateAllFragmentDatas(boolean updateProgressBar) {
        if (entryId > 0) {
            long timeBeforeRemove;
            // Load datas from the db and put them at the right place
            entryDetails = dbManager.getEntryDetails(entryId);

            put.setText(Utils.convertDateIntoReadable(entryDetails.getDatePut().split(" ")[0], false) + "\n" + entryDetails.getDatePut().split(" ")[1]);

            // Choose color if the timeWeared is enough or not
            // Depending of the timeWeared set in the settings
            if (!entryDetails.getIsRunning()) {
                if ((entryDetails.getTimeWeared() - SessionsManager.computeTotalTimePause(dbManager, entryId)) / 60 >= weared_time)
                    textview_progress.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                else
                    textview_progress.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else
                textview_progress.setTextColor(getResources().getColor(R.color.yellow));

            // Check if the session is finished and display the corresponding text
            // Either 'Not set yet', saying the session is not over
            // Or the endSession date
            Log.d(TAG, "Compute total time pause is " + SessionsManager.computeTotalTimePause(dbManager, entryId));

            textview_total_time.setText(String.format("/ %s", Utils.convertTimeWeared(settingsManager.getWearingTimeInt() * 60)));

            total_breaks.setText(String.valueOf(pausesDatas.size()));
            total_time_breaks.setText(Utils.convertTimeWeared(SessionsManager.computeTotalTimePause(dbManager, entryId)));

            // Display the datas relative to the session
            if (entryDetails.getIsRunning()) {
                timeBeforeRemove = Utils.getDateDiff(entryDetails.getDatePut(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES) - SessionsManager.computeTotalTimePause(dbManager, entryId);

                removed.setText(entryDetails.getDateRemoved());
                textview_progress.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));

                //isRunning.setTextColor(getResources().getColor(R.color.yellow));
                //isRunning.setText(R.string.session_is_running);

                stopSessionButton.setVisibility(View.VISIBLE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Utils.getdateParsed(entryDetails.getDatePut()));
                calendar.add(Calendar.HOUR_OF_DAY, weared_time);
                updateAbleToGetItOffUI(calendar);

                int progress_percentage = (int) (((float) timeBeforeRemove / (float) (settingsManager.getWearingTimeInt() * 60)) * 100);

                textview_percentage_progression.setText(String.format("%d%%", progress_percentage));
                end_session.setVisibility(View.GONE);
                estimated_end.setVisibility(View.VISIBLE);
            } else {
                timeBeforeRemove = Utils.getDateDiff(entryDetails.getDatePut(), entryDetails.getDateRemoved(), TimeUnit.MINUTES) - SessionsManager.computeTotalTimePause(dbManager, entryId);
                Log.d(TAG, "TimeBeforeRemove is " + timeBeforeRemove);
                removed.setText(Utils.convertDateIntoReadable(entryDetails.getDateRemoved().split(" ")[0], false) + "\n" + entryDetails.getDateRemoved().split(" ")[1]);
                int time_spent_wearing = entryDetails.getTimeWeared();
                if (time_spent_wearing < 60)
                    textview_progress.setText(entryDetails.getTimeWeared() + getString(R.string.minute_with_M_uppercase));
                else
                    textview_progress.setText(String.format("%dh%02dm", time_spent_wearing / 60, time_spent_wearing % 60));

                // If the session is finished, no need to show the ableToGetItOff textView.
                // This textview is only used to warn user when he will be able to get it off
                //isRunning.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                //isRunning.setText(R.string.session_finished);
                whenGetItOff.setVisibility(View.GONE);
                stopSessionButton.setVisibility(View.GONE);
                estimated_end.setVisibility(View.GONE);
                end_session.setVisibility(View.VISIBLE);
            }
            Log.d(TAG, "Preference is " + (float) settingsManager.getWearingTimeInt());
            Log.d(TAG, "timeBeforeRemove is " + (float)timeBeforeRemove);

            Log.d(TAG, "MainView percentage is " + (int) (((float) timeBeforeRemove / (float) (settingsManager.getWearingTimeInt() * 60)) * 100));
            if (updateProgressBar) {
                float progress_percentage = ((float) timeBeforeRemove / (float) (MainActivity.getSettingsManager().getWearingTimeInt() * 60)) * 100;
                if (progress_percentage > 100f)
                    progressBar.setIndicatorColor(context.getResources().getColor(R.color.green_main_bar));
                else
                    progressBar.setIndicatorColor(context.getResources().getColor(R.color.blue_main_bar));
                progressBar.setProgress((int)progress_percentage);
            }
            //Log.d(TAG, "Progress is supposed to be at " + progressBar.getProgress());
            recomputeWearingTime();
            updatePauseList();
        } else {
            // Trigger an error if the entryId is wrong, then go back to main list
            Toast.makeText(context, context.getString(R.string.error_bad_id_entry_details) + entryId, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: Wrong Id: " + entryId);
            requireActivity().onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllFragmentDatas(true);
    }
}
