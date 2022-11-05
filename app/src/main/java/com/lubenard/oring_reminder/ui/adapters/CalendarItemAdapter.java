package com.lubenard.oring_reminder.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lubenard.oring_reminder.R;

import java.util.ArrayList;

public class CalendarItemAdapter extends BaseAdapter {

    private ArrayList<String> dayList;
    private Context context;
    private int calendarOffset;
    private CalendarItemAdapter.onListItemClickListener onListItemClickListener;

    public CalendarItemAdapter(Context context, ArrayList<String> dayList, int calendarOffset, CalendarItemAdapter.onListItemClickListener onListItemClickListener) {
        this.dayList = dayList;
        this.context = context;
        this.calendarOffset = calendarOffset;
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

        ((TextView)gridItem.findViewById(R.id.calendar_grid_item_layout)).setText(String.valueOf(position + 1));
        return gridItem;
    }

    public interface onListItemClickListener {
        void onListItemClickListener(int position);
    }
}
