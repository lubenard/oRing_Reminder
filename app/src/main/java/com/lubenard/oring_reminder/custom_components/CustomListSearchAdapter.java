package com.lubenard.oring_reminder.custom_components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CustomListSearchAdapter extends ArrayAdapter<RingSession> {

    private static class ViewHolder {
        TextView worn_date;
        TextView weared_from;
        TextView weared_to;
        TextView weared_during;
    }

    public CustomListSearchAdapter(ArrayList<RingSession> data, Context context) {
        super(context, R.layout.main_history_one_elem, data);
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
        viewHolder.weared_from = convertView.findViewById(R.id.custom_view_date_weared_from);
        viewHolder.weared_to = convertView.findViewById(R.id.custom_view_date_weared_to);
        viewHolder.weared_during = convertView.findViewById(R.id.custom_view_date_time_weared);

        // Forced to split with a space because the date format is YYYY-MM-dd hh:MM:ss
        String[] datePut = dataModel.getDatePut().split(" ");
        viewHolder.worn_date.setText(Utils.convertDateIntoReadable(datePut[0]));

        viewHolder.weared_from.setText(datePut[1]);

        if (!dataModel.getDateRemoved().equals("NOT SET YET")) {
            String[] dateRemoved = dataModel.getDateRemoved().split(" ");
            viewHolder.worn_date.setText(Utils.convertDateIntoReadable(datePut[0]) + " -> " + Utils.convertDateIntoReadable(dateRemoved[0]));
            viewHolder.weared_to.setText(dateRemoved[1]);
        } else
            viewHolder.weared_to.setText(dataModel.getDateRemoved());

        if (dataModel.getIsRunning() == 0) {
            int totalTimePause = getTotalTimePause(dataModel.getDatePut(), dataModel.getId(), dataModel.getDateRemoved());
            if (totalTimePause / 60 >= 15)
                viewHolder.weared_during.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
            else
                viewHolder.weared_during.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
            viewHolder.weared_during.setText(convertTimeWeared(totalTimePause));
        }
        else {
            long timeBeforeRemove = getTotalTimePause(dataModel.getDatePut(), dataModel.getId(), null);
            viewHolder.weared_during.setTextColor(getContext().getResources().getColor(R.color.yellow));
            viewHolder.weared_during.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
        }
        return convertView;
    }
}
