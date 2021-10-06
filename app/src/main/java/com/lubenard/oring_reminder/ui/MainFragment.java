package com.lubenard.oring_reminder.ui;

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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {
    ProgressBar progress_bar;
    TextView progress_bar_text;
    private FloatingActionButton fab;
    private View view;
    private static boolean orderEntryByDesc = true;

    private static ViewGroup viewGroup;
    private ArrayList<RingModel> dataModels;
    private DbManager dbManager;
    private TextView test;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.new_design_home_fragment, container, false);
    }

    private void updateHistoryList() {
        viewGroup.removeAllViews();
        dataModels.clear();
        ArrayList<RingModel> entrysDatas = dbManager.getHistoryForMainView(orderEntryByDesc);

        LayoutInflater inflater = (LayoutInflater) getActivity().
                getSystemService(getContext().LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i != entrysDatas.size(); i++) {
            View view = inflater.inflate(R.layout.main_history_one_elem, null);
            view.setTag(Integer.toString((int) entrysDatas.get(i).getId()));

            TextView textView_date = view.findViewById(R.id.main_history_date);
            textView_date.setText(entrysDatas.get(i).getDatePut().split(" ")[0]);

            TextView textView_hour_from = view.findViewById(R.id.custom_view_date_weared_to);
            textView_hour_from.setText(entrysDatas.get(i).getDatePut().split(" ")[1]);

            TextView textView_hour_to = view.findViewById(R.id.custom_view_date_weared_from);
            textView_hour_to.setText(entrysDatas.get(i).getDateRemoved().split(" ")[1]);

            TextView textView_worn_for = view.findViewById(R.id.custom_view_date_time_weared);
            int totalTimePause = getTotalTimePause(entrysDatas.get(i).getDatePut(), entrysDatas.get(i).getId(), entrysDatas.get(i).getDateRemoved());
            if (totalTimePause / 60 >= 15)
                textView_worn_for.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
            else
                textView_worn_for.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
            textView_worn_for.setText(convertTimeWeared(totalTimePause));

            viewGroup.addView(view);

            //view.setOnClickListener(clickInLinearLayout());
        }
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
        updateDesign();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
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
            case R.id.action_settings:
                // Navigate to settings screen
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new SettingsFragment(), null)
                        .addToBackStack(null).commit();
                return true;
            case R.id.action_reload_datas:
                updateCurrSessionDatas();
                updateHistoryList();
                return true;
            case R.id.action_sort_entrys:
                orderEntryByDesc = !orderEntryByDesc;
                Toast.makeText(getContext(), getContext().getString((orderEntryByDesc) ? R.string.ordered_by_desc : R.string.not_ordered_by_desc), Toast.LENGTH_SHORT).show();
                updateHistoryList();
                return true;
            default:
                return false;
        }
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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

    private void updateCurrSessionDatas() {
        RingModel lastRunningEntry = dbManager.getLastRunningEntry();

        if (lastRunningEntry != null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            long timeBeforeRemove = getTotalTimePause(lastRunningEntry.getDatePut(), lastRunningEntry.getId(), null);
            test.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
            Log.d("Main view", "MainView percentage is " + ((float) timeBeforeRemove / (float) (Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")) * 60)) * 100);
            progress_bar.setProgress((int) (((float) timeBeforeRemove / (float) (Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")) * 60)) * 100));
        }
    }

    private void updateDesign() {
        // If this return null, mean there is no running session
        if (dbManager.getLastRunningEntry() == null) {

            LinearLayout linearLayout = view.findViewById(R.id.layout_session_active);
            linearLayout.setVisibility(View.GONE);

            TextView no_active_session = view.findViewById(R.id.layout_no_session_active);
            no_active_session.setVisibility(View.VISIBLE);

            ImageButton see_curr_session = view.findViewById(R.id.see_current_session);
            see_curr_session.setVisibility(View.INVISIBLE);

            fab.setBackgroundTintList(ColorStateList.valueOf(getContext().getResources().getColor(R.color.teal_700)));
            fab.setImageDrawable(getResources().getDrawable(R.drawable.baseline_add_24));

            fab.setOnClickListener(view12 -> actionOnPlusButton(false));

            fab.setOnLongClickListener(view1 -> {
                actionOnPlusButton(true);
                return true;
            });
        } else {
            LinearLayout linearLayout = view.findViewById(R.id.layout_session_active);
            linearLayout.setVisibility(View.VISIBLE);

            TextView no_active_session = view.findViewById(R.id.layout_no_session_active);
            no_active_session.setVisibility(View.GONE);

            ImageButton see_curr_session = view.findViewById(R.id.see_current_session);
            see_curr_session.setVisibility(View.VISIBLE);

            fab.setBackgroundTintList(ColorStateList.valueOf(getContext().getResources().getColor(android.R.color.holo_red_dark)));
            fab.setImageDrawable(getResources().getDrawable(R.drawable.outline_close_24));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbManager.endSession(dbManager.getLastRunningEntry().getId());
                    updateDesign();
                }
            });
        }
        updateHistoryList();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbManager = MainActivity.getDbManager();
        dataModels = new ArrayList<>();

        viewGroup = view.findViewById(R.id.list_history);

        progress_bar = view.findViewById(R.id.progress_bar);
        progress_bar_text = view.findViewById(R.id.text_view_progress);

        test = view.findViewById(R.id.text_view_progress);

        fab = view.findViewById(R.id.fab);

        this.view = view;

        getActivity().setTitle(R.string.app_name);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        updateDesign();
    }
}
