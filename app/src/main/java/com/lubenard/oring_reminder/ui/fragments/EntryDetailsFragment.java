package com.lubenard.oring_reminder.ui.fragments;

import static android.os.Build.VERSION.SDK_INT;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBreaksBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EntryDetailsFragment extends Fragment {

    public static final String TAG = "EntryDetailsFragment";

    private long entryId = -1;
    private DbManager dbManager;
    private int weared_time;
    private View view;
    private Context context;
    private FragmentManager fragmentManager;
    private ArrayList<RingSession> pausesDatas;
    private int newAlarmDate;
    private RingSession entryDetails;
    private TextView ableToGetItOff;
    private TextView whenGetItOff;
    private TextView textview_progress;
    private FloatingActionButton stopSessionButton;
    private boolean isThereAlreadyARunningPause = false;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private static ViewGroup viewGroup;

    private MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_entry_details, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            int id = menuItem.getItemId();
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

        viewGroup = view.findViewById(R.id.listview_pauses);

        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        weared_time = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));

        stopSessionButton = view.findViewById(R.id.button_finish_session);

        stopSessionButton.setOnClickListener(view13 -> {
            dbManager.endSession(entryId);
            updateAllFragmentDatas(false);
            EditEntryFragment.updateWidget(context);
        });

        ImageButton pauseButton = view.findViewById(R.id.new_pause_button);
        pauseButton.setOnClickListener(view1 -> showPauseAlertDialog(null, -1));
        pauseButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (isThereAlreadyARunningPause) {
                    Log.d(TAG, "Error: Already a running pause");
                    Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
                } else if (entryDetails.getIsRunning()) {
                    String date = Utils.getdateFormatted(new Date());
                    long id = dbManager.createNewPause(entryId, date, "NOT SET YET", 1);
                    // Cancel alarm until breaks are set as finished.
                    // Only then set a new alarm date
                    Log.d(TAG, "Cancelling alarm for entry: " + entryId);
                    EditEntryFragment.cancelAlarm(context, entryId);
                    setBreakAlarm(sharedPreferences, Utils.getdateFormatted(new Date()), context, entryId);
                    createNewBreak(id, date, "NOT SET YET", 1);
                    updatePauseList();
                    EditEntryFragment.updateWidget(getContext());
                } else
                    Toast.makeText(context, R.string.no_pause_session_is_not_running, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        requireActivity().addMenuProvider(menuProvider);
    }

    private void createNewBreak(long id, String startDate, String endDate, int isRunning) {
        pausesDatas.add(0, new RingSession((int)id, endDate, startDate, isRunning, 0));
    }

    /**
     * Show user pause alert dialog
     * Also compute if pause it in the session interval
     * @param dataModel If the pause already exist, give it datas to load
     * @param position inside the pauseDatas Array. Will be ignored if dataModel is Null
     */
    private void showPauseAlertDialog(RingSession dataModel, int position) {
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
            String pauseEndingText = pause_ending.getText().toString();
            String pauseBeginningText = pause_beginning.getText().toString();
            if (pauseEndingText.isEmpty() || pauseEndingText.equals("NOT SET YET")) {
                pauseEndingText = "NOT SET YET" ;
                isRunning = 1;
            }

            if (isThereAlreadyARunningPause && isRunning == 1) {
                Log.d(TAG, "Error: Already a running pause");
                Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
            } else if (Utils.getDateDiff(entryDetails.getDatePut(), pauseBeginningText, TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: Start of pause < start of entry");
                Toast.makeText(context, context.getString(R.string.pause_beginning_to_small), Toast.LENGTH_SHORT).show();
            } else if (isRunning == 0 && Utils.getDateDiff(entryDetails.getDatePut(), pauseEndingText, TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: End of pause < start of entry");
                Toast.makeText(context, context.getString(R.string.pause_ending_too_small), Toast.LENGTH_SHORT).show();
            } else if (isRunning == 0 && !entryDetails.getIsRunning() && Utils.getDateDiff(pauseEndingText, entryDetails.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: End of pause > end of entry");
                Toast.makeText(context, context.getString(R.string.pause_ending_too_big), Toast.LENGTH_SHORT).show();
            } else if (!entryDetails.getIsRunning() && Utils.getDateDiff(pauseBeginningText, entryDetails.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
                Log.d(TAG, "Error: Start of pause > end of entry");
                Toast.makeText(context, context.getString(R.string.pause_starting_too_big), Toast.LENGTH_SHORT).show();
            } else {
                if (dataModel == null) {
                    long id = dbManager.createNewPause(entryId, pauseBeginningText, pauseEndingText, isRunning);
                    createNewBreak(id, pauseBeginningText, pauseEndingText, isRunning);
                } else {
                    long id = dbManager.updatePause(dataModel.getId(), pauseBeginningText, pauseEndingText, isRunning);
                    long timeWorn = Utils.getDateDiff(pauseBeginningText, pauseEndingText, TimeUnit.MINUTES);
                    pausesDatas.set(position, new RingSession((int)id, pauseEndingText, pauseBeginningText, isRunning, (int)timeWorn));
                    // Cancel the break notification if it is set as finished.
                    if (isRunning == 0) {
                        Intent intent = new Intent(getContext(), NotificationSenderBreaksBroadcastReceiver.class).putExtra("action", 1);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), (int) ((dataModel != null) ? dataModel.getId() : entryId), intent, PendingIntent.FLAG_MUTABLE);
                        AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                        am.cancel(pendingIntent);
                    }
                }
                alertDialog.dismiss();
                recomputeWearingTime();

                // Only recompute alarm if session is running, else cancel it.
                if (entryDetails.getIsRunning()) {
                    if (pause_ending.getText().toString().equals("NOT SET YET")) {
                        Log.d(TAG, "Cancelling alarm for entry: " + entryId);
                        EditEntryFragment.cancelAlarm(context, entryId);
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(Utils.getdateParsed(entryDetails.getDatePut()));
                        calendar.add(Calendar.MINUTE, newAlarmDate);
                        Log.d(TAG, "Setting alarm for entry: " + entryId + " At: " + Utils.getdateFormatted(calendar.getTime()));
                        // Cancel break alarm is session is set as finished
                        if (sharedPreferences.getBoolean("myring_prevent_me_when_pause_too_long", false)) {
                            Intent intent = new Intent(getContext(), NotificationSenderBreaksBroadcastReceiver.class)
                                    .putExtra("action", 1);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), (int) entryId, intent, 0);
                            AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                            am.cancel(pendingIntent);
                        }
                        EditEntryFragment.setAlarm(context, Utils.getdateFormatted(calendar.getTime()), entryId, true);
                    }
                }
                if (isRunning == 1)
                    setBreakAlarm(sharedPreferences, pause_beginning.getText().toString(), context, entryId);
                updatePauseList();
                EditEntryFragment.updateWidget(getContext());
            }
        });
        alertDialog.show();
    }

    /**
     * Add alarm if break is too long (only if break is running and option enabled in settings)
     * @param pauseBeginning
     */
    public static void setBreakAlarm(SharedPreferences sharedPreferences, String pauseBeginning, Context context, long entryId) {
        if (sharedPreferences.getBoolean("myring_prevent_me_when_pause_too_long", false)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Utils.getdateParsed(pauseBeginning));
            calendar.add(Calendar.MINUTE, sharedPreferences.getInt("myring_prevent_me_when_pause_too_long_date", 0));
            Log.d(TAG, "Setting break alarm at " + Utils.getdateFormatted(calendar.getTime()));
            Intent intent = new Intent(context, NotificationSenderBreaksBroadcastReceiver.class)
                    .putExtra("action", 1);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            if (SDK_INT < Build.VERSION_CODES.M)
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            else
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private View.OnClickListener clickInLinearLayout() {
        return v -> {
            Integer position = Integer.parseInt(v.getTag().toString());
            Log.d(TAG, "Clicked item at position: " + position);

            showPauseAlertDialog(pausesDatas.get(position), position);
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
        ableToGetItOff.setText(getString(R.string._message_able_to_get_it_off) + Utils.convertDateIntoReadable(ableToGetItOffStringDate[0], false) + " " + ableToGetItOffStringDate[1]);
        if (timeBeforeRemove >= 0)
            texteRessourceWhenGetItOff = R.string.in_about_entry_details;
        else {
            texteRessourceWhenGetItOff = R.string.when_get_it_off_negative;
            timeBeforeRemove *= -1;
        }
        whenGetItOff.setText(String.format(getString(texteRessourceWhenGetItOff), timeBeforeRemove / 60, timeBeforeRemove % 60));
    }

    /**
     * Convert the timeWeared from a int into a readable hour:minutes format
     * @param timeWeared timeWeared is in minutes
     * @return a string containing the time the user weared the protection
     */
    private String convertTimeWeared(int timeWeared) {
        if (timeWeared < 60)
            return timeWeared + getContext().getString(R.string.minute_with_M_uppercase);
        else
            return String.format("%dh%02dm", timeWeared / 60, timeWeared % 60);
    }

    /**
     * Update the listView by fetching all elements from the db
     */
    private void updatePauseList() {
        viewGroup.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getActivity().
                getSystemService(getContext().LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i != pausesDatas.size(); i++) {
            View view = inflater.inflate(R.layout.main_history_one_elem, null);
            view.setTag(Integer.toString(i));

            String[] dateRemoved = pausesDatas.get(i).getDateRemoved().split(" ");

            TextView wornForTextView = view.findViewById(R.id.worn_for_history);
            wornForTextView.setText(R.string.removed_during);

            TextView textView_date = view.findViewById(R.id.main_history_date);
            textView_date.setText(Utils.convertDateIntoReadable(dateRemoved[0], false));

            TextView textView_hour_from = view.findViewById(R.id.custom_view_date_weared_from);
            textView_hour_from.setText(dateRemoved[1]);

            TextView textView_hour_to = view.findViewById(R.id.custom_view_date_weared_to);

            TextView textView_worn_for = view.findViewById(R.id.custom_view_date_time_weared);

            if (!pausesDatas.get(i).getIsRunning()) {
                String[] datePut = pausesDatas.get(i).getDatePut().split(" ");
                textView_hour_to.setText(datePut[1]);
                if (!dateRemoved[0].equals(datePut[0]))
                    textView_date.setText(Utils.convertDateIntoReadable(dateRemoved[0], false) + " -> " + Utils.convertDateIntoReadable(datePut[0], false));
                textView_worn_for.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                textView_worn_for.setText(convertTimeWeared(pausesDatas.get(i).getTimeWeared()));
            } else {
                long timeworn = Utils.getDateDiff(pausesDatas.get(i).getDateRemoved(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                textView_worn_for.setTextColor(getContext().getResources().getColor(R.color.yellow));
                textView_worn_for.setText(String.format("%dh%02dm", timeworn / 60, timeworn % 60));
                textView_hour_to.setText("Not set yet");
            }

            view.setOnClickListener(clickInLinearLayout());
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(context).setTitle(R.string.alertdialog_delete_entry)
                            .setMessage(R.string.alertdialog_delete_contact_body)
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                Integer position = Integer.parseInt(v.getTag().toString());
                                RingSession object = (RingSession) pausesDatas.get(position);
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
                                    EditEntryFragment.setAlarm(context, Utils.getdateFormatted(calendar.getTime()), entryId, true);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                    return true;
                }
            });
            viewGroup.addView(view);
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
            TextView put = view.findViewById(R.id.details_entry_put);
            TextView removed = view.findViewById(R.id.details_entry_removed);
            textview_progress = view.findViewById(R.id.text_view_progress);
            TextView isRunning = view.findViewById(R.id.details_entry_isRunning);
            ableToGetItOff = view.findViewById(R.id.details_entry_able_to_get_it_off);
            whenGetItOff = view.findViewById(R.id.details_entry_when_get_it_off);
            //progressBar = view.findViewById(R.id.progress_bar);

            put.setText(Utils.convertDateIntoReadable(entryDetails.getDatePut().split(" ")[0], false) + " " + entryDetails.getDatePut().split(" ")[1]);

            // Choose color if the timeWeared is enough or not
            // Depending of the timeWeared set in the settings
            if (!entryDetails.getIsRunning()) {
                if ((entryDetails.getTimeWeared() - AfterBootBroadcastReceiver.computeTotalTimePause(dbManager, entryId)) / 60 >= weared_time)
                    textview_progress.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                else
                    textview_progress.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else
                textview_progress.setTextColor(getResources().getColor(R.color.yellow));

            // Check if the session is finished and display the corresponding text
            // Either 'Not set yet', saying the session is not over
            // Or the endSession date
            Log.d(TAG, "Compute total time pause is " + AfterBootBroadcastReceiver.computeTotalTimePause(dbManager, entryId));

            // Display the datas relative to the session
            if (entryDetails.getIsRunning()) {
                timeBeforeRemove = Utils.getDateDiff(entryDetails.getDatePut(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES) - AfterBootBroadcastReceiver.computeTotalTimePause(dbManager, entryId);

                removed.setText(entryDetails.getDateRemoved());
                textview_progress.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));

                isRunning.setTextColor(getResources().getColor(R.color.yellow));
                isRunning.setText(R.string.session_is_running);

                stopSessionButton.setVisibility(View.VISIBLE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Utils.getdateParsed(entryDetails.getDatePut()));
                calendar.add(Calendar.HOUR_OF_DAY, weared_time);
                updateAbleToGetItOffUI(calendar);
            } else {
                timeBeforeRemove = Utils.getDateDiff(entryDetails.getDatePut(), entryDetails.getDateRemoved(), TimeUnit.MINUTES) - AfterBootBroadcastReceiver.computeTotalTimePause(dbManager, entryId);
                Log.d(TAG, "TimeBeforeRemove is " + timeBeforeRemove);
                removed.setText(Utils.convertDateIntoReadable(entryDetails.getDateRemoved().split(" ")[0], false) + " " + entryDetails.getDateRemoved().split(" ")[1]);
                int time_spent_wearing = entryDetails.getTimeWeared();
                if (time_spent_wearing < 60)
                    textview_progress.setText(entryDetails.getTimeWeared() + getString(R.string.minute_with_M_uppercase));
                else
                    textview_progress.setText(String.format("%dh%02dm", time_spent_wearing / 60, time_spent_wearing % 60));

                // If the session is finished, no need to show the ableToGetItOff textView.
                // This textview is only used to warn user when he will be able to get it off
                isRunning.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                isRunning.setText(R.string.session_finished);
                ableToGetItOff.setVisibility(View.GONE);
                whenGetItOff.setVisibility(View.GONE);
                stopSessionButton.setVisibility(View.GONE);
            }
            Log.d(TAG, "Preference is " + (float) (Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"))));
            Log.d(TAG, "timeBeforeRemove is " + (float)timeBeforeRemove);

            Log.d(TAG, "MainView percentage is " + (int) (((float) timeBeforeRemove / (float) (Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")) * 60)) * 100));
            if (updateProgressBar) {
                //progressBar.setProgressDrawable(null);
                //progressBar.setProgressDrawable(context.getDrawable(R.drawable.calendar_circle_red));
                //progressBar.setProgress((int) (((float) timeBeforeRemove / (float) (Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")) * 60)) * 100));
            }
            //Log.d(TAG, "Progress is supposed to be at " + progressBar.getProgress());
            recomputeWearingTime();
            updatePauseList();
        } else {
            // Trigger an error if the entryId is wrong, then go back to main list
            Toast.makeText(context, context.getString(R.string.error_bad_id_entry_details) + entryId, Toast.LENGTH_SHORT);
            Log.e(TAG, "Error: Wrong Id: " + entryId);
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllFragmentDatas(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "OnDestroyView called");
        requireActivity().removeMenuProvider(menuProvider);
    }
}
