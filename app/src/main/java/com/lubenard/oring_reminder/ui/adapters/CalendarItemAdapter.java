package com.lubenard.oring_reminder.ui.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CalendarItemAdapter extends BaseAdapter {

    private ArrayList<String> dayList;
    private Context context;
    private SharedPreferences sharedPreferences;
    private int calendarOffset;
    private HashMap<Integer, RingSession> monthEntries;
    private CalendarItemAdapter.onListItemClickListener onListItemClickListener;

    public CalendarItemAdapter(Context context, ArrayList<String> dayList, HashMap<Integer, RingSession> monthEntries, int calendarOffset, CalendarItemAdapter.onListItemClickListener onListItemClickListener) {
        this.dayList = dayList;
        this.monthEntries = monthEntries;
        this.context = context;
        this.calendarOffset = calendarOffset;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.onListItemClickListener = onListItemClickListener;
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

        if (!dayList.get(position).equals("0")) {

            RingSession session = monthEntries.get(position);

            Log.d("CalendarItemAdapter", "session found is " + session);

            ((TextView) gridItem.findViewById(R.id.calendar_grid_item_layout)).setText(String.valueOf(position));

            if (session != null) {
                if (session.getIsRunning() == 1)
                    ((TextView) gridItem.findViewById(R.id.calendar_grid_item_layout)).setBackground(context.getResources().getDrawable(R.drawable.calendar_circle_yellow));
                else {
                    if (session.getTimeWeared() >= (Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15")) * 60))
                        ((TextView) gridItem.findViewById(R.id.calendar_grid_item_layout)).setBackground(context.getResources().getDrawable(R.drawable.calendar_circle_green));
                    else
                        ((TextView) gridItem.findViewById(R.id.calendar_grid_item_layout)).setBackground(context.getResources().getDrawable(R.drawable.calendar_circle_red));
                }
            }
        }
        return gridItem;
    }

    public interface onListItemClickListener {
        void onListItemClickListener(int position);
    }
}
