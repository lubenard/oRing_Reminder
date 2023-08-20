package com.lubenard.oring_reminder.pages.home;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.pages.calculator.CalculatorsFragment;
import com.lubenard.oring_reminder.pages.datas.DatasFragment;
import com.lubenard.oring_reminder.pages.entry_details.EntryDetailsFragment;
import com.lubenard.oring_reminder.pages.my_spermograms.MySpermogramsFragment;
import com.lubenard.oring_reminder.pages.search.SearchFragment;
import com.lubenard.oring_reminder.ui.fragments.EditEntryFragment;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private ConstraintLayout activeSessionLayout;
    private LinearLayout noActiveSessionLayout;
    private Context context;
    private static FragmentActivity activity;
    private static HomeViewModel homeViewModel;
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
                    requireActivity().removeMenuProvider(menuProvider);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new MySpermogramsFragment(), null)
                            .addToBackStack(null).commit();
                    return true;
                case R.id.action_calculators:
                    new CalculatorsFragment().show(requireActivity().getSupportFragmentManager(), null);
                    return true;
                case R.id.action_datas:
                    requireActivity().removeMenuProvider(menuProvider);
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
                    requireActivity().removeMenuProvider(menuProvider);
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
        getChildFragmentManager().setFragmentResultListener("EditEntryFragmentResult", this, (requestKey, bundle1) -> {
            boolean result = bundle1.getBoolean("shouldUpdateParent", true);
            Log.d(TAG, "got result from fragment: " + result);
            if (result)
                updateDesign();
        });
        fragment.show(getChildFragmentManager(), null);
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
                Toast.makeText(context, "Session started at: " + DateUtils.getdateFormatted(new Date()), Toast.LENGTH_SHORT).show();
                SessionsManager.insertNewEntry(context, DateUtils.getdateFormatted(new Date()));
                updateDesign();
            }
        } else {
            if (action.equals("default")) {
                Toast.makeText(context, "Session started at: " + DateUtils.getdateFormatted(new Date()), Toast.LENGTH_SHORT).show();
                SessionsManager.insertNewEntry(context, DateUtils.getdateFormatted(new Date()));
                updateDesign();
            } else {
                startEditEntryFragment();
            }
        }
    }

    /**
     * Update whole design on MainFragment, including fab
     */
    public void updateDesign() {
        homeViewModel.getCurrentSession();
        homeViewModel.computeWearingTimeSinceMidnight();
        homeViewModel.getLast24hWearingTime();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        Log.d(TAG, "vm is " + homeViewModel);

        if (homeViewModel.shouldUpdateDbInstance)
            homeViewModel.updateDbManager();

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

        activeSessionLayout = view.findViewById(R.id.layout_session_active);
        noActiveSessionLayout = view.findViewById(R.id.layout_no_session_active);

        updateDesign();

        homeViewModel.wearingTimeSinceMidnight.observe(getViewLifecycleOwner(), wearingTimeSinceMidgnight -> {
            home_since_midnight_data.setText(DateUtils.convertTimeWeared(wearingTimeSinceMidgnight));
        });

        homeViewModel.last24hWearingTime.observe(getViewLifecycleOwner(), last24hWearingTime -> {
            home_last_24h_data.setText(DateUtils.convertTimeWeared(last24hWearingTime));
        });

        homeViewModel.currentSession.observe(getViewLifecycleOwner(), currentSession -> {
            if (currentSession == null) {
                activeSessionLayout.setVisibility(View.GONE);

                noActiveSessionLayout.setVisibility(View.VISIBLE);

                fab.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.green)));
                fab.setImageDrawable(context.getDrawable(R.drawable.baseline_add_24));

                fab.setOnClickListener(view12 -> actionOnPlusButton(false));

                fab.setOnLongClickListener(view1 -> {
                    actionOnPlusButton(true);
                    return true;
                });
            } else {
                Log.d(TAG, "Current Session loaded, currentSession is " + currentSession.getIsRunning());
                activeSessionLayout.setVisibility(View.VISIBLE);

                noActiveSessionLayout.setVisibility(View.GONE);

                fab.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(android.R.color.holo_red_dark)));
                fab.setImageDrawable(context.getDrawable(R.drawable.outline_close_24));

                fab.setOnClickListener(v -> {
                    homeViewModel.endSession();
                });

                button_see_curr_session.setOnClickListener(v -> {
                    activity.removeMenuProvider(menuProvider);
                    EntryDetailsFragment fragment = new EntryDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong("entryId", dbManager.getLastRunningEntry().getId());
                    fragment.setArguments(bundle);
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, fragment, null)
                            .addToBackStack(null).commit();
                });

                long timeBeforeRemove = SessionsManager.getWearingTimeWithoutPause(currentSession.getDatePut(), currentSession.getId(), null);
                textview_progress.setText(DateUtils.convertTimeWeared((int)timeBeforeRemove));
                time_needed_to_complete_session.setText(String.format("/ %s", DateUtils.convertTimeWeared(MainActivity.getSettingsManager().getWearingTimeInt() * 60)));
                float progress_percentage = ((float) timeBeforeRemove / (float) (MainActivity.getSettingsManager().getWearingTimeInt() * 60)) * 100;

                String[] splittedDatePut = currentSession.getDatePut().split(" ");

                start_session_data.setText(String.format(context.getString(R.string.formatted_datetime), DateUtils.convertDateIntoReadable(splittedDatePut[0], false), splittedDatePut[1]));

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentSession.getDatePutCalendar().getTime());
                calendar.add(Calendar.HOUR_OF_DAY, MainActivity.getSettingsManager().getWearingTimeInt());

                String[] splittedDateEstimatedEnd = DateUtils.getdateFormatted(calendar.getTime()).split(" ");

                estimated_end_session_data.setText(String.format(context.getString(R.string.formatted_datetime), DateUtils.convertDateIntoReadable(splittedDateEstimatedEnd[0], false), splittedDateEstimatedEnd[1]));

                Log.d(TAG, "MainView percentage is " + progress_percentage);
                if (progress_percentage < 1f)
                    progress_percentage = 1f;
                if (progress_percentage > 100f)
                    progress_bar.setIndicatorColor(context.getResources().getColor(R.color.green_main_bar));
                else
                    progress_bar.setIndicatorColor(context.getResources().getColor(R.color.blue_main_bar));
                progress_bar.setProgress((int)progress_percentage);

                homeViewModel.isThereARunningBreak.observe(getViewLifecycleOwner(), isThereARunningBreak -> {
                    if (isThereARunningBreak) {
                        text_view_break.setText(String.format("%s: %d mn", context.getString(R.string.in_break_for), DateUtils.getDateDiff(homeViewModel.sessionBreaks.getValue().get(0).getStartDate(), DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES)));
                        text_view_break.setVisibility(View.VISIBLE);
                        button_start_break.setText(context.getString(R.string.widget_stop_break));
                        button_start_break.setOnClickListener(v -> homeViewModel.endBreak(RingSession.SessionStatus.RUNNING));
                    } else {
                        text_view_break.setVisibility(View.INVISIBLE);
                        button_start_break.setText(context.getString(R.string.widget_start_break));
                        button_start_break.setOnClickListener(v -> homeViewModel.startBreak(context));
                    }
                });
            }
        });

        this.view = view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView() called");
        Log.d(TAG, "onDestroy activity is " + activity.toString() + ", menuProvider: " + menuProvider);
        homeViewModel.stopTimer();
        homeViewModel.resetInstanceDB();
        activity.removeMenuProvider(menuProvider);
        super.onDestroyView();
    }
}
