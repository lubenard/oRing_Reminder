package com.lubenard.oring_reminder.ui.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import com.lubenard.oring_reminder.utils.Log;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private static CircularProgressIndicator progress_bar;
    private Button button_start_break;
    private static FloatingActionButton fab;
    private TextView text_view_break;
    private View view;
    private Button button_see_curr_session;
    private TextView time_needed_to_complete_session;

    private ArrayList<RingSession> dataModels;
    private static DbManager dbManager;
    private TextView textview_progress;
    private TextView home_since_midnight_data;
    private TextView home_last_24h_data;
    private TextView start_session_data;
    private TextView estimated_end_session_data;
    private Context context;
    private static FragmentActivity activity;

    private final MenuProvider menuProvider = new MenuProvider() {
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
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new MySpermogramsFragment(), null)
                            .addToBackStack(null).commit();
                    return true;
                case R.id.action_calculators:
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new CalculatorsFragment(), null)
                            .addToBackStack(null).commit();
                    return true;
                case R.id.action_datas:
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new DatasFragment(), null)
                            .addToBackStack(null).commit();
                    return true;
                default:
                    return false;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreateView()");
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        updateDesign();
    }

    /**
     * Compute all pause time into interval
     * @param entryId entry for the wanted session
     * @param date24HoursAgo oldest boundaries
     * @param dateNow interval newest boundaries
     * @return the time in Minutes of pauses between the interval
     */
    public static int computeTotalTimePauseForId(long entryId, String date24HoursAgo, String dateNow) {
        ArrayList<BreakSession> pausesDatas = dbManager.getAllBreaksForId(entryId, true);
        int totalTimePause = 0;
        for (int i = 0; i < pausesDatas.size(); i++) {
            BreakSession currentBreak = pausesDatas.get(i);
            Log.d(TAG, "BreakSession is " + currentBreak);
            if (!pausesDatas.get(i).getIsRunning()) {
                if (Utils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) > 0 &&
                        Utils.getDateDiff(currentBreak.getEndDate(), dateNow, TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is added: " + pausesDatas.get(i).getTimeRemoved());
                    totalTimePause += currentBreak.getTimeRemoved();
                } else if (Utils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) <= 0 &&
                        Utils.getDateDiff(date24HoursAgo, currentBreak.getEndDate(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is between the born: " + Utils.getDateDiff(date24HoursAgo, currentBreak.getEndDate(), TimeUnit.SECONDS));
                    totalTimePause += Utils.getDateDiff(date24HoursAgo, currentBreak.getEndDate(), TimeUnit.MINUTES);
                }
            } else {
                if (Utils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running pause at index " + i + " is added: " + Utils.getDateDiff(currentBreak.getStartDate(), dateNow, TimeUnit.SECONDS));
                    totalTimePause += Utils.getDateDiff(currentBreak.getStartDate(), dateNow, TimeUnit.MINUTES);
                } else if (Utils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running pause at index " + i + " is between the born: " + Utils.getDateDiff(date24HoursAgo, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimePause += Utils.getDateDiff(date24HoursAgo, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                }
            }
        }
        return totalTimePause;
    }

    private static int getSinceMidnightWearingTime() {
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
    private static int getLast24hWearingTime() {
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
    private static void startEditEntryFragment() {
        EditEntryFragment fragment = new EditEntryFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("entryId", -1);
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), null);
    }

    /**
     * Define what action should be done on longClick on the '+' button
     * @param isLongClick act if it is a long click or not
     */
    private void actionOnPlusButton(boolean isLongClick) {
        String action = MainActivity.getSettingsManager().getActionUIFab();

        if (isLongClick) {
            if (action.equals("default")) {
                startEditEntryFragment();
            } else {
                Toast.makeText(context, "Session started at: " + Utils.getdateFormatted(new Date()), Toast.LENGTH_SHORT).show();
                SessionsManager.insertNewEntry(context, Utils.getdateFormatted(new Date()));
                updateDesign();
            }
        } else {
            if (action.equals("default")) {
                Toast.makeText(context, "Session started at: " + Utils.getdateFormatted(new Date()), Toast.LENGTH_SHORT).show();
                SessionsManager.insertNewEntry(context, Utils.getdateFormatted(new Date()));
                updateDesign();
            } else {
                startEditEntryFragment();
            }
        }
    }

    private void updateCurrSessionDatas() {
        RingSession lastRunningEntry = dbManager.getLastRunningEntry();

        if (lastRunningEntry != null) {
            long timeBeforeRemove = SessionsManager.getWearingTimeWithoutPause(lastRunningEntry.getDatePut(), lastRunningEntry.getId(), null);
            textview_progress.setText(Utils.convertTimeWeared((int)timeBeforeRemove));
            time_needed_to_complete_session.setText(String.format("/ %s", Utils.convertTimeWeared(MainActivity.getSettingsManager().getWearingTimeInt() * 60)));
            float progress_percentage = ((float) timeBeforeRemove / (float) (MainActivity.getSettingsManager().getWearingTimeInt() * 60)) * 100;

            String[] splittedDatePut = lastRunningEntry.getDatePut().split(" ");

            start_session_data.setText(String.format(context.getString(R.string.formatted_datetime), Utils.convertDateIntoReadable(splittedDatePut[0], false), splittedDatePut[1]));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Utils.getdateParsed(lastRunningEntry.getDatePut()));
            calendar.add(Calendar.HOUR_OF_DAY, MainActivity.getSettingsManager().getWearingTimeInt());

            String[] splittedDateEstimatedEnd = Utils.getdateFormatted(calendar.getTime()).split(" ");

            estimated_end_session_data.setText(String.format(context.getString(R.string.formatted_datetime), Utils.convertDateIntoReadable(splittedDateEstimatedEnd[0], false), splittedDateEstimatedEnd[1]));

            Log.d(TAG, "MainView percentage is " + progress_percentage);
            if (progress_percentage < 1f)
                progress_percentage = 1f;
            if (progress_percentage > 100f)
                progress_bar.setIndicatorColor(context.getResources().getColor(R.color.green_main_bar));
            else
                progress_bar.setIndicatorColor(context.getResources().getColor(R.color.blue_main_bar));
            progress_bar.setProgress((int)progress_percentage);

            if (dbManager.getAllBreaksForId(lastRunningEntry.getId(), true).size() > 0 &&
                dbManager.getAllBreaksForId(lastRunningEntry.getId(), true).get(0).getIsRunning()) {
                text_view_break.setText(String.format("%s: %d mn", context.getString(R.string.in_break_for) ,Utils.getDateDiff(dbManager.getLastRunningPauseForId(lastRunningEntry.getId()).getStartDate(), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES)));
                text_view_break.setVisibility(View.VISIBLE);
                button_start_break.setText(context.getString(R.string.widget_stop_break));
                button_start_break.setOnClickListener(v -> {
                    dbManager.endPause(lastRunningEntry.getId());
                    updateCurrSessionDatas();
                });
            } else {
                text_view_break.setVisibility(View.INVISIBLE);
                button_start_break.setText(context.getString(R.string.widget_start_break));
                button_start_break.setOnClickListener(v -> {
                    SessionsManager.startBreak(context);
                    updateCurrSessionDatas();
                });
            }
        }
    }

    /**
     * Update whole design on MainFragment, including fab
     */
    public void updateDesign() {

        int totalTimeSinceMidnight = getSinceMidnightWearingTime();
        home_since_midnight_data.setText(Utils.convertTimeWeared(totalTimeSinceMidnight));

        int totalTimeLastDay = getLast24hWearingTime();
        home_last_24h_data.setText(Utils.convertTimeWeared(totalTimeLastDay));

        // If this return null, mean there is no running session
        if (dbManager.getLastRunningEntry() == null) {
            ConstraintLayout linearLayout = view.findViewById(R.id.layout_session_active);
            linearLayout.setVisibility(View.GONE);

            LinearLayout no_active_session = view.findViewById(R.id.layout_no_session_active);
            no_active_session.setVisibility(View.VISIBLE);

            fab.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.teal_700)));
            fab.setImageDrawable(context.getDrawable(R.drawable.baseline_add_24));

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

            fab.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(android.R.color.holo_red_dark)));
            fab.setImageDrawable(context.getDrawable(R.drawable.outline_close_24));

            fab.setOnClickListener(v -> {
                dbManager.endSession(dbManager.getLastRunningEntry().getId());
                updateDesign();
            });

            button_see_curr_session.setOnClickListener(v -> {
                EntryDetailsFragment fragment = new EntryDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putLong("entryId", dbManager.getLastRunningEntry().getId());
                fragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, fragment, null)
                        .addToBackStack(null).commit();
            });

            updateCurrSessionDatas();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = requireContext();
        activity = requireActivity();

        activity.setTitle(R.string.app_name);
        ((AppCompatActivity)activity).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        Log.d(TAG, "onCreate activity is " + activity.toString() + ", menuProvider: " + menuProvider);
        activity.addMenuProvider(menuProvider);

        Log.d(TAG, "onViewCreated()");

        dbManager = MainActivity.getDbManager();
        dataModels = new ArrayList<>();

        progress_bar = view.findViewById(R.id.progress_bar_main);

        textview_progress = view.findViewById(R.id.text_view_progress);

        fab = view.findViewById(R.id.fab);

        text_view_break = view.findViewById(R.id.text_view_break);
        button_start_break = view.findViewById(R.id.button_start_break);

        home_since_midnight_data = view.findViewById(R.id.home_since_midnight_data);
        home_last_24h_data = view.findViewById(R.id.home_last_24h_data);

        button_see_curr_session = view.findViewById(R.id.see_current_session);
        time_needed_to_complete_session = view.findViewById(R.id.on_needed_time_to_complete);

        start_session_data = view.findViewById(R.id.start_session_data);
        estimated_end_session_data = view.findViewById(R.id.estimated_end_data);

        this.view = view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView() called");
        Log.d(TAG, "onDestroy activity is " + activity.toString() + ", menuProvider: " + menuProvider);

        activity.removeMenuProvider(menuProvider);
        super.onDestroyView();
    }
}
