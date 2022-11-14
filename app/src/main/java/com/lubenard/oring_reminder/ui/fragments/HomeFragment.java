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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private CircularProgressIndicator progress_bar;
    private TextView progress_bar_text;
    private Button button_start_break;
    private FloatingActionButton fab;
    private TextView text_view_break;
    private View view;

    private ArrayList<RingSession> dataModels;
    private DbManager dbManager;
    private TextView textview_progress;
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
    private void createNewEntry() {
        EditEntryFragment fragment = new EditEntryFragment(getContext());
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
        String action = sharedPreferences.getString("ui_action_on_plus_button", "default");

        if (isLongClick) {
            if (action.equals("default")) {
                createNewEntry();
            } else {
                Toast.makeText(getContext(), "Session started at: " + Utils.getdateFormatted(new Date()), Toast.LENGTH_SHORT).show();
                //EditEntryFragment.setUpdateMainList(true);
                new EditEntryFragment(getContext()).insertNewEntry(Utils.getdateFormatted(new Date()), false);
                updateDesign();
            }
        } else {
            if (action.equals("default")) {
                Toast.makeText(getContext(), "Session started at: " + Utils.getdateFormatted(new Date()), Toast.LENGTH_SHORT).show();
                //EditEntryFragment.setUpdateMainList(true);
                new EditEntryFragment(getContext()).insertNewEntry(Utils.getdateFormatted(new Date()), false);
                updateDesign();
            } else {
                createNewEntry();
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

    /**
     * Start break on MainFragment
     */
    private void startBreak() {
        RingSession lastRunningEntry = dbManager.getLastRunningEntry();

        if (dbManager.getLastRunningPauseForId(lastRunningEntry.getId()) == null) {
            Log.d(TAG, "No running pause");
            dbManager.createNewPause(lastRunningEntry.getId(), Utils.getdateFormatted(new Date()), "NOT SET YET", 1);
            // Cancel alarm until breaks are set as finished.
            // Only then set a new alarm date
            Log.d(TAG, "Cancelling alarm for entry: " + lastRunningEntry.getId());
            EditEntryFragment.cancelAlarm(getContext(), lastRunningEntry.getId());
            EntryDetailsFragment.setBreakAlarm(PreferenceManager.getDefaultSharedPreferences(getContext()),
                    Utils.getdateFormatted(new Date()), getContext(), lastRunningEntry.getId());
            EditEntryFragment.updateWidget(getContext());
        } else {
            Log.d(TAG, "Error: Already a running pause");
            Toast.makeText(getContext(), getContext().getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCurrSessionDatas() {
        RingSession lastRunningEntry = dbManager.getLastRunningEntry();

        if (lastRunningEntry != null) {
            long timeBeforeRemove = getTotalTimePause(lastRunningEntry.getDatePut(), lastRunningEntry.getId(), null);
            textview_progress.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
            float progress_percentage = ((float) timeBeforeRemove / (float) (Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")) * 60)) * 100;
            Log.d(TAG, "MainView percentage is " + progress_percentage);
            if (progress_percentage < 1f)
                progress_percentage = 1f;
            if (progress_percentage > 100f)
                progress_bar.setIndicatorColor(getResources().getColor(R.color.green_main_bar));
            else
                progress_bar.setIndicatorColor(getResources().getColor(R.color.blue_main_bar));
            progress_bar.setProgress((int)progress_percentage);
            if (dbManager.getAllPausesForId(lastRunningEntry.getId(), true).size() > 0 &&
                dbManager.getAllPausesForId(lastRunningEntry.getId(), true).get(0).getIsRunning() == 1) {
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
                    startBreak();
                    updateCurrSessionDatas();
                });
            }
        }
    }

    /**
     * Update whole design on MainFragment, including fab
     */
    private void updateDesign() {
        // If this return null, mean there is no running session
        if (dbManager.getLastRunningEntry() == null) {

            ScrollView linearLayout = view.findViewById(R.id.layout_session_active);
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
            ScrollView linearLayout = view.findViewById(R.id.layout_session_active);
            linearLayout.setVisibility(View.VISIBLE);

            LinearLayout no_active_session = view.findViewById(R.id.layout_no_session_active);
            no_active_session.setVisibility(View.GONE);

            fab.setBackgroundTintList(ColorStateList.valueOf(getContext().getResources().getColor(android.R.color.holo_red_dark)));
            fab.setImageDrawable(getContext().getDrawable(R.drawable.outline_close_24));
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

        dbManager = MainActivity.getDbManager();
        dataModels = new ArrayList<>();

        progress_bar = view.findViewById(R.id.progress_bar_main);
        progress_bar_text = view.findViewById(R.id.text_view_progress);

        textview_progress = view.findViewById(R.id.text_view_progress);

        fab = view.findViewById(R.id.fab);

        text_view_break = view.findViewById(R.id.text_view_break);
        button_start_break = view.findViewById(R.id.button_start_break);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        this.view = view;

        getActivity().setTitle(R.string.app_name);

        requireActivity().addMenuProvider(menuProvider);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "OnDestroyView called");
        requireActivity().removeMenuProvider(menuProvider);
    }
}
