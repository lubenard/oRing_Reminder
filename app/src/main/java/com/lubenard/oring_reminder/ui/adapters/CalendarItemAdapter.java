package com.lubenard.oring_reminder.ui.adapters;

import android.content.Context;

import com.lubenard.oring_reminder.ui.fragments.EntryDetailsFragment;
import com.lubenard.oring_reminder.ui.fragments.SearchFragment;
import com.lubenard.oring_reminder.utils.Log;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.SettingsManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarItemAdapter extends BaseAdapter {

    private static final String TAG = "CalendarItemAdapter";

    private ArrayList<String> dayList;
    private Context context;
    private FragmentActivity activity;
    private SettingsManager settingsManager;
    private int calendarOffset;
    // Variables used to display today mark if today is in current month.
    // It's value is either -1 if not present, or [1..31] if present
    private int todayIndex;
    private Calendar date;
    private List<Pair<Integer, RingSession>> monthEntries;

    public CalendarItemAdapter(FragmentActivity activity, Context context, ArrayList<String> dayList, List<Pair<Integer, RingSession>> monthEntries, int calendarOffset, int todayCounter, Calendar date) {
        this.dayList = dayList;
        this.monthEntries = monthEntries;
        this.context = context;
        this.todayIndex = todayCounter;
        this.calendarOffset = calendarOffset;
        this.activity = activity;
        this.date = date;
        this.settingsManager = new SettingsManager(context);
    }

    @Override
    public int getCount() {
        return dayList.size();
    }

    @Override
    public Object getItem(int i) {
        return dayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridItem;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        gridItem = inflater.inflate(R.layout.calendar_grid_item, null);

        Log.d("CalendarItemAdapter", "Iterate over  " + dayList.get(position) + " with position " + position);

        if (!dayList.get(position).equals("0")) {

            TextView numberTextView = gridItem.findViewById(R.id.calendar_grid_item_layout);

            numberTextView.setText(dayList.get(position));

            List<RingSession> sessions = filterSessions(monthEntries, Integer.parseInt(dayList.get(position)));

            Log.d(TAG, "Sessions for " + dayList.get(position) + " are size " + sessions.size());

            if (todayIndex != -1 && todayIndex == Integer.parseInt(dayList.get(position)))
                numberTextView.setTextColor(context.getResources().getColor(android.R.color.holo_blue_light));

            if (sessions.size() > 0) {
                RingSession session = sessions.get(sessions.size() - 1);
                Log.d("CalendarItemAdapter", "session found is " + session);

                if (session != null) {
                    if (session.getIsRunning())
                        numberTextView.setBackground(context.getResources().getDrawable(R.drawable.calendar_circle_yellow));
                    else {
                        if (session.getTimeWeared() >= (settingsManager.getWearingTimeInt() * 60))
                            numberTextView.setBackground(context.getResources().getDrawable(R.drawable.calendar_circle_green));
                        else
                            numberTextView.setBackground(context.getResources().getDrawable(R.drawable.calendar_circle_red));
                    }

                    numberTextView.setOnClickListener(v -> {
                        Log.d(TAG, "Clicked on item " + dayList.get(position));
                        if (sessions.size() > 1) {
                            SearchFragment fragment = new SearchFragment();
                            Bundle bundle = new Bundle();
                            String day = dayList.get(position);
                            if (Integer.parseInt(dayList.get(position)) < 10)
                                day = "0" + dayList.get(position);
                            bundle.putString("date_searched", date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) + 1) + "-" + day);
                            fragment.setArguments(bundle);
                            activity.getSupportFragmentManager().beginTransaction()
                                    .replace(android.R.id.content, fragment, null)
                                    .addToBackStack(null).commit();
                        } else {
                            Log.d(TAG, "Launching EntryDetailsFragment");
                            Bundle bundle = new Bundle();
                            bundle.putLong("entryId", session.getId());
                            Fragment fragment = new EntryDetailsFragment();
                            fragment.setArguments(bundle);
                            activity.getSupportFragmentManager().beginTransaction()
                                    .replace(android.R.id.content, fragment, null)
                                    .addToBackStack(null).commit();
                        }
                    });
                }
            }
        }
        return gridItem;
    }

    private List<RingSession> filterSessions(List<Pair<Integer, RingSession>> sessions, int day) {
        List<RingSession> filteredSessions = new ArrayList<>();

        for (Pair<Integer, RingSession> session : sessions) {
            if (session.first == day)
                filteredSessions.add(session.second);
        }

        return filteredSessions;
    }


    @Override
    public boolean isEnabled(int position) {
        Log.d(TAG, "is " + dayList.get(position) + " enabled ? answer is " + (!dayList.get(position).equals("0") && filterSessions(monthEntries, Integer.parseInt(dayList.get(position))).size() > 0));
        return !dayList.get(position).equals("0") && filterSessions(monthEntries, Integer.parseInt(dayList.get(position))).size() > 0;
    }
}
