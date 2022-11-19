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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private CircularProgressIndicator progress_bar;
    private TextView progress_bar_text;
    private Button button_start_break;
    private FloatingActionButton fab;
    private TextView text_view_break;
    private View view;
    private Button button_see_curr_session;

    private ArrayList<RingSession> dataModels;
    private DbManager dbManager;
    private TextView textview_progress;
    private TextView home_since_midnight_data;
    private TextView home_last_24h_data;
    private SharedPreferences sharedPreferences;

    private MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            int id = menuItem.getItemId();
            switch (id) {
                case R.id.action_search_entry:
                    searchEntry();
                    return true;
                case R.id.action_my_spermogramms:
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new MySpermogramsFragment(), null)
                            .addToBackStack(null).commit();
                    return true;
                case R.id.action_calculators:
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new CalculatorsFragment(), null)
                            .addToBackStack(null).commit();
                    return true;
                case R.id.action_datas:
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new DatasFragment(), null)
                            .addToBackStack(null).commit();
                    return true;
                case R.id.action_reload_datas:
                    updateCurrSessionDatas();
                    return true;
                default:
                    return false;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
        updateDesign();
    }

    /**
     * Compute all pause time into interval
     * @param entryId entry for the wanted session
     * @param date24HoursAgo oldest boundaries
     * @param dateNow interval newest boundaries
     * @return the time in Minutes of pauses between the interval
     */
    public int computeTotalTimePauseForId(long entryId, String date24HoursAgo, String dateNow) {
        ArrayList<RingSession> pausesDatas = dbManager.getAllPausesForId(entryId, true);
        int totalTimePause = 0;
        for (int i = 0; i < pausesDatas.size(); i++) {
            RingSession currentBreak = pausesDatas.get(i);
            if (!pausesDatas.get(i).getIsRunning()) {
                if (Utils.getDateDiff(date24HoursAgo, currentBreak.getDateRemoved(), TimeUnit.SECONDS) > 0 &&
                        Utils.getDateDiff(currentBreak.getDatePut(), dateNow, TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is added: " + pausesDatas.get(i).getTimeWeared());
                    totalTimePause += currentBreak.getTimeWeared();
                } else if (Utils.getDateDiff(date24HoursAgo, currentBreak.getDateRemoved(), TimeUnit.SECONDS) <= 0 &&
                        Utils.getDateDiff(date24HoursAgo, currentBreak.getDatePut(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is between the born: " + Utils.getDateDiff(date24HoursAgo, currentBreak.getDatePut(), TimeUnit.SECONDS));
                    totalTimePause += Utils.getDateDiff(date24HoursAgo, currentBreak.getDatePut(), TimeUnit.MINUTES);
                }
            } else {
                if (Utils.getDateDiff(date24HoursAgo, currentBreak.getDateRemoved(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running pause at index " + i + " is added: " + Utils.getDateDiff(currentBreak.getDateRemoved(), dateNow, TimeUnit.SECONDS));
                    totalTimePause += Utils.getDateDiff(currentBreak.getDateRemoved(), dateNow, TimeUnit.MINUTES);
                } else if (Utils.getDateDiff(date24HoursAgo, currentBreak.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running pause at index " + i + " is between the born: " + Utils.getDateDiff(date24HoursAgo, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimePause += Utils.getDateDiff(date24HoursAgo, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                }
            }
        }
        return totalTimePause;
    }

    private int getSinceMidnightWearingTime() {
        int totalTimeSinceMidnight = 0;
        int pauseTimeForThisEntry;

        LinkedHashMap<Integer, RingSession> entrysDatas = dbManager.getAllDatasForMainList(true);
        ArrayList<RingSession> dataModels = new ArrayList<>(entrysDatas.values());

        Calendar calendar = Calendar.getInstance();
        String todayDate = Utils.getdateFormatted(calendar.getTime());

        // We set the calendar at midnight
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        String sinceMidnigt = Utils.getdateFormatted(calendar.getTime());
        Log.d(TAG, "Computing since midnight: interval is between: " + sinceMidnigt + " and " + todayDate);

        RingSession currentModel;
        for (int i = 0; i != (Math.min(dataModels.size(), 5)); i++) {
            currentModel = dataModels.get(i);
            pauseTimeForThisEntry = computeTotalTimePauseForId(currentModel.getId(), sinceMidnigt, todayDate);
            Log.d(TAG, "Session id: " + currentModel.getId()
                    + ", pauseTimeForThisEntry " + pauseTimeForThisEntry
                    + ", getDatePut: " + currentModel.getDatePut()
                    + ", datediff datePut: " + Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) + " seconds, ");
            if (!currentModel.getIsRunning()) {
                if (Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) > 0 &&
                        Utils.getDateDiff(currentModel.getDateRemoved(), todayDate, TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is fully in the last 24h (start and end inside 'now' and 'now - 24h')
                    Log.d(TAG, "entry at index " + i + " added " + dataModels.get(i).getTimeWeared() + " to counter");
                    totalTimeSinceMidnight += currentModel.getTimeWeared() - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0 &&
                        Utils.getDateDiff(sinceMidnigt, currentModel.getDateRemoved(),  TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is half beetween interval (start before before 24h ago and end after interval start)
                    Log.d(TAG, "entry at index " + i + " is between the born: " + Utils.getDateDiff(sinceMidnigt, currentModel.getDateRemoved(), TimeUnit.SECONDS));
                    totalTimeSinceMidnight += Utils.getDateDiff(sinceMidnigt, currentModel.getDateRemoved(), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            } else {
                if (Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running entry at index " + i + " is added: " + Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.SECONDS));
                    totalTimeSinceMidnight += Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.MINUTES) - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running entry at index " + i + " is between the born: " + Utils.getDateDiff(sinceMidnigt, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimeSinceMidnight += Utils.getDateDiff(sinceMidnigt, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            }
        }
        Log.d(TAG, "Computed last since midnight is: " + totalTimeSinceMidnight + "mn");
        return totalTimeSinceMidnight;
    }

    /**
     * Get all session wearing time with the breaks removed for the last 24 hours.
     * @return time in minute of worn time on the last 24 hours
     */
    private int getLast24hWearingTime() {
        int totalTimeLastDay = 0;
        int pauseTimeForThisEntry;

        LinkedHashMap<Integer, RingSession> entrysDatas = dbManager.getAllDatasForMainList(true);
        ArrayList<RingSession> dataModels = new ArrayList<>(entrysDatas.values());

        Calendar calendar = Calendar.getInstance();
        String todayDate = Utils.getdateFormatted(calendar.getTime());

        // We set the calendar 24h earlier
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        String oneDayEarlier = Utils.getdateFormatted(calendar.getTime());
        Log.d(TAG, "Computing last 24 hours: interval is between: " + oneDayEarlier + " and " + todayDate);

        RingSession currentModel;
        for (int i = 0; i != (Math.min(dataModels.size(), 5)); i++) {
            currentModel = dataModels.get(i);
            pauseTimeForThisEntry = computeTotalTimePauseForId(currentModel.getId(), oneDayEarlier, todayDate);
            Log.d(TAG, "Session id: " + currentModel.getId()
                    + ", pauseTimeForThisEntry " + pauseTimeForThisEntry
                    + ", getDatePut: " + currentModel.getDatePut()
                    + ", datediff datePut: " + Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) + " seconds, ");
            if (!currentModel.getIsRunning()) {
                if (Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) > 0 &&
                        Utils.getDateDiff(currentModel.getDateRemoved(), todayDate, TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is fully in the last 24h (start and end inside 'now' and 'now - 24h')
                    Log.d(TAG, "entry at index " + i + " added " + dataModels.get(i).getTimeWeared() + " to counter");
                    totalTimeLastDay += currentModel.getTimeWeared() - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0 &&
                        Utils.getDateDiff(oneDayEarlier, currentModel.getDateRemoved(),  TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is half beetween interval (start before before 24h ago and end after interval start)
                    Log.d(TAG, "entry at index " + i + " is between the born: " + Utils.getDateDiff(oneDayEarlier, currentModel.getDateRemoved(), TimeUnit.SECONDS));
                    totalTimeLastDay += Utils.getDateDiff(oneDayEarlier, currentModel.getDateRemoved(), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            } else {
                if (Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running entry at index " + i + " is added: " + Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.SECONDS));
                    totalTimeLastDay += Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.MINUTES) - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running entry at index " + i + " is between the born: " + Utils.getDateDiff(oneDayEarlier, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimeLastDay += Utils.getDateDiff(oneDayEarlier, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            }
        }
        Log.d(TAG, "Computed last 24 hours is: " + totalTimeLastDay + "mn");
        return totalTimeLastDay;
    }

    private void searchEntry() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    SearchFragment fragment = new SearchFragment();
                    Bundle bundle = new Bundle();
                    String monthString = String.valueOf(monthOfYear + 1);
                    String dayString = String.valueOf(dayOfMonth);

                    if (monthOfYear < 10)
                        monthString = "0" + monthString;

                    if (dayOfMonth < 10)
                        dayString = "0" + dayString;
                    bundle.putString("date_searched", year + "-" + monthString + "-" + dayString);
                    fragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, fragment, null)
                            .addToBackStack(null).commit();
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    /**
     * Launch the new Entry fragment, and specify we do not want to update a entry
     */
    private void startEditEntryFragment() {
        EditEntryFragment fragment = new EditEntryFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("entryId", -1);
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, null)
                .addToBackStack(null).commit();
    }

    /**
     * Define what action should be done on longClick on the '+' button
     * @param isLongClick act if it is a long click or not
     */
    private void actionOnPlusButton(boolean isLongClick) {
        String action = new SettingsManager(getContext()).getActionUIFab();

        if (isLongClick) {
            if (action.equals("default")) {
                startEditEntryFragment();
            } else {
                Toast.makeText(getContext(), "Session started at: " + Utils.getdateFormatted(new Date()), Toast.LENGTH_SHORT).show();
                SessionsManager.insertNewEntry(getContext(), Utils.getdateFormatted(new Date()));
                updateDesign();
            }
        } else {
            if (action.equals("default")) {
                Toast.makeText(getContext(), "Session started at: " + Utils.getdateFormatted(new Date()), Toast.LENGTH_SHORT).show();
                SessionsManager.insertNewEntry(getContext(), Utils.getdateFormatted(new Date()));
                updateDesign();
            } else {
                startEditEntryFragment();
            }
        }
    }

    /**
     * Get the total time pause for one session
     * @param datePut The datetime the user put the protection
     * @param entryId the entry id of the session
     * @param dateRemoved The datetime the user removed the protection
     * @return the total time in Minutes of new wearing time
     */
    private int getTotalTimePause(String datePut, long entryId, String dateRemoved) {
        long oldTimeBeforeRemove;
        int newValue;
        long totalTimePause = 0;

        if (dateRemoved == null)
            oldTimeBeforeRemove = Utils.getDateDiff(datePut, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
        else
            oldTimeBeforeRemove = Utils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES);

        totalTimePause = AfterBootBroadcastReceiver.computeTotalTimePause(MainActivity.getDbManager(), entryId);
        newValue = (int) (oldTimeBeforeRemove - totalTimePause);
        return (newValue < 0) ? 0 : newValue;
    }

    private void updateCurrSessionDatas() {
        RingSession lastRunningEntry = dbManager.getLastRunningEntry();

        if (lastRunningEntry != null) {
            long timeBeforeRemove = getTotalTimePause(lastRunningEntry.getDatePut(), lastRunningEntry.getId(), null);
            textview_progress.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
            float progress_percentage = ((float) timeBeforeRemove / (float) (new SettingsManager(getContext()).getWearingTimeInt() * 60)) * 100;
            Log.d(TAG, "MainView percentage is " + progress_percentage);
            if (progress_percentage < 1f)
                progress_percentage = 1f;
            if (progress_percentage > 100f)
                progress_bar.setIndicatorColor(getResources().getColor(R.color.green_main_bar));
            else
                progress_bar.setIndicatorColor(getResources().getColor(R.color.blue_main_bar));
            progress_bar.setProgress((int)progress_percentage);

            if (dbManager.getAllPausesForId(lastRunningEntry.getId(), true).size() > 0 &&
                dbManager.getAllPausesForId(lastRunningEntry.getId(), true).get(0).getIsRunning()) {
                text_view_break.setText(String.format("%s: %d mn", getString(R.string.in_break_for) ,Utils.getDateDiff(dbManager.getLastRunningPauseForId(lastRunningEntry.getId()).getDateRemoved(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES)));
                text_view_break.setVisibility(View.VISIBLE);
                button_start_break.setText(getString(R.string.widget_stop_break));
                button_start_break.setOnClickListener(v -> {
                    dbManager.endPause(lastRunningEntry.getId());
                    updateCurrSessionDatas();
                });
            } else {
                text_view_break.setVisibility(View.INVISIBLE);
                button_start_break.setText(getString(R.string.widget_start_break));
                button_start_break.setOnClickListener(v -> {
                    SessionsManager.startBreak(getContext());
                    updateCurrSessionDatas();
                });
            }
        }
    }

    /**
     * Update whole design on MainFragment, including fab
     */
    private void updateDesign() {

        int totalTimeSinceMidnight = getSinceMidnightWearingTime();
        home_since_midnight_data.setText(String.format("%dh%02dm", totalTimeSinceMidnight / 60, totalTimeSinceMidnight % 60));

        int totalTimeLastDay = getLast24hWearingTime();
        home_last_24h_data.setText(String.format("%dh%02dm", totalTimeLastDay / 60, totalTimeLastDay % 60));

        // If this return null, mean there is no running session
        if (dbManager.getLastRunningEntry() == null) {

            ConstraintLayout linearLayout = view.findViewById(R.id.layout_session_active);
            linearLayout.setVisibility(View.GONE);

            LinearLayout no_active_session = view.findViewById(R.id.layout_no_session_active);
            no_active_session.setVisibility(View.VISIBLE);

            fab.setBackgroundTintList(ColorStateList.valueOf(getContext().getResources().getColor(R.color.teal_700)));
            fab.setImageDrawable(getContext().getDrawable(R.drawable.baseline_add_24));

            fab.setOnClickListener(view12 -> actionOnPlusButton(false));

            fab.setOnLongClickListener(view1 -> {
                actionOnPlusButton(true);
                return true;
            });
        } else {
            ConstraintLayout linearLayout = view.findViewById(R.id.layout_session_active);
            linearLayout.setVisibility(View.VISIBLE);

            LinearLayout no_active_session = view.findViewById(R.id.layout_no_session_active);
            no_active_session.setVisibility(View.GONE);

            fab.setBackgroundTintList(ColorStateList.valueOf(getContext().getResources().getColor(android.R.color.holo_red_dark)));
            fab.setImageDrawable(getContext().getDrawable(R.drawable.outline_close_24));

            button_see_curr_session.setOnClickListener(v -> {
                EntryDetailsFragment fragment = new EntryDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putLong("entryId", dbManager.getLastRunningEntry().getId());
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, fragment, null)
                        .addToBackStack(null).commit();
            });

            updateCurrSessionDatas();
            fab.setOnClickListener(v -> {
                dbManager.endSession(dbManager.getLastRunningEntry().getId());
                updateDesign();
            });
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(R.string.app_name);
        requireActivity().addMenuProvider(menuProvider);

        dbManager = MainActivity.getDbManager();
        dataModels = new ArrayList<>();

        progress_bar = view.findViewById(R.id.progress_bar_main);
        progress_bar_text = view.findViewById(R.id.text_view_progress);

        textview_progress = view.findViewById(R.id.text_view_progress);

        fab = view.findViewById(R.id.fab);

        text_view_break = view.findViewById(R.id.text_view_break);
        button_start_break = view.findViewById(R.id.button_start_break);

        home_since_midnight_data = view.findViewById(R.id.home_since_midnight_data);
        home_last_24h_data = view.findViewById(R.id.home_last_24h_data);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        button_see_curr_session = view.findViewById(R.id.see_current_session);

        this.view = view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView called");
        requireActivity().removeMenuProvider(menuProvider);
        super.onDestroyView();
    }
}
