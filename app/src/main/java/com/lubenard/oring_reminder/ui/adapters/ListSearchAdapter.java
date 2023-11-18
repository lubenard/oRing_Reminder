package com.lubenard.oring_reminder.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.utils.DateUtils;

import java.util.List;

public class ListSearchAdapter extends ArrayAdapter<RingSession> {

    private final static String TAG = "ListSearchAdapter";

    private static class ViewHolder {
        TextView worn_date;
        TextView weared_from;
        TextView weared_to;
        TextView weared_during;
    }

    public ListSearchAdapter(List<RingSession> data, Context context) {
        super(context, R.layout.main_history_one_elem, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RingSession dataModel = getItem(position);
        ViewHolder viewHolder;

        // Get our layout and Textview.
        // Inflate it and get all elements
        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.main_history_one_elem, parent, false);
        viewHolder.worn_date = convertView.findViewById(R.id.main_history_date);
        viewHolder.weared_from = convertView.findViewById(R.id.history_listview_item_hour_from);
        viewHolder.weared_to = convertView.findViewById(R.id.history_listview_item_hour_to);
        viewHolder.weared_during = convertView.findViewById(R.id.history_listview_item_time_weared);

        // Forced to split with a space because the date format is YYYY-MM-dd hh:MM:ss
        String[] datePut = dataModel.getDatePut().split(" ");
        viewHolder.worn_date.setText(DateUtils.convertDateIntoReadable(datePut[0], false));
        viewHolder.weared_from.setText(datePut[1]);

        if (dataModel.getIsRunning()) {
            viewHolder.weared_to.setText("");

            long timeBeforeRemove = dataModel.getSessionDuration() - dataModel.computeTotalTimePause();
            viewHolder.weared_during.setTextColor(getContext().getResources().getColor(R.color.yellow));
            viewHolder.weared_during.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
        } else {
            String[] dateRemoved = dataModel.getDateRemoved().split(" ");
            if (!datePut[0].equals(dateRemoved[0]))
                viewHolder.worn_date.setText(String.format("%s -> %s", DateUtils.convertDateIntoReadable(datePut[0], false), DateUtils.convertDateIntoReadable(dateRemoved[0], false)));
            else
                viewHolder.worn_date.setText(DateUtils.convertDateIntoReadable(datePut[0], false));

            viewHolder.weared_to.setText(dateRemoved[1]);

            int totalTimePause = dataModel.getSessionDuration();
            if (totalTimePause >= MainActivity.getSettingsManager().getWearingTimeInt())
                viewHolder.weared_during.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
            else
                viewHolder.weared_during.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
            viewHolder.weared_during.setText(DateUtils.convertIntIntoReadableDate(totalTimePause));
        }
        return convertView;
    }
}
