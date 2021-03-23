package com.lubenard.oring_reminder.custom_components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CustomListAdapter extends ArrayAdapter<RingModel> {

    private static class ViewHolder {
        TextView weared_from;
        TextView weared_to;
        TextView weared_during;
    }

    public CustomListAdapter(ArrayList<RingModel> data, Context context) {
        super(context, R.layout.custom_contact_list_element, data);
    }

    /**
     * Convert the timeWeared from a int into a readable hour:minutes format
     * @param timeWeared timeWeared is in minutes
     * @return a string containing the time the user weared the protection
     */
    private String convertTimeWeared(int timeWeared) {
        if (timeWeared < 60)
            return timeWeared + getContext().getString(R.string.minute_with_M_uppercase);
        else if (timeWeared <= 1440)
            return String.format("%dh%02dm", timeWeared / 60, timeWeared % 60);
        else
            return getContext().getString(R.string.more_than_one_day);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RingModel dataModel = getItem(position);
        ViewHolder viewHolder;

        // Get our layout and Textview.
        // Inflate it and get all elements
        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.custom_contact_list_element, parent, false);
        viewHolder.weared_from = convertView.findViewById(R.id.custom_view_date_weared_from);
        viewHolder.weared_to = convertView.findViewById(R.id.custom_view_date_weared_to);
        viewHolder.weared_during = convertView.findViewById(R.id.custom_view_date_time_weared);

        // Forced to split with a space because the date format is YYYY-MM-dd hh:MM:ss
        String[] datePut = dataModel.getDatePut().split(" ");
        viewHolder.weared_from.setText(datePut[0] + "\n" + datePut[1]);

        if (!dataModel.getDateRemoved().equals("NOT SET YET")) {
            String[] dateRemoved = dataModel.getDateRemoved().split(" ");
            viewHolder.weared_to.setText(dateRemoved[0] + "\n" + dateRemoved[1]);
        } else {
            viewHolder.weared_to.setText(dataModel.getDateRemoved());
        }
        if (dataModel.getIsRunning() == 0) {
            viewHolder.weared_during.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
            viewHolder.weared_during.setText(convertTimeWeared(dataModel.getTimeWeared()));
        }
        else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long timeBeforeRemove = Utils.getDateDiff(dataModel.getDatePut(), dateFormat.format(new Date()), TimeUnit.MINUTES);
            viewHolder.weared_during.setTextColor(getContext().getResources().getColor(R.color.yellow));
            viewHolder.weared_during.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
        }


        return convertView;
    }
}
