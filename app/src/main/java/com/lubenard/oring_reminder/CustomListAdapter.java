package com.lubenard.oring_reminder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<RingModel> implements View.OnClickListener {

    @Override
    public void onClick(View view) {
    }

    // View lookup cache
    private static class ViewHolder {
        TextView weared_from;
        TextView weared_to;
        TextView weared_during;
    }

    public CustomListAdapter(ArrayList<RingModel> data, Context context) {
        super(context, R.layout.custom_contact_list_element, data);
    }


    private String convertTimeWeared(int timeWeared) {
        if (timeWeared < 60) {
            return timeWeared + " Minutes";
        } else if (timeWeared <= 1440) {
            return String.format("%dh%02dm", timeWeared / 60, timeWeared % 60);
        } else {
            return "> 1 day";
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RingModel dataModel = getItem(position);
        ViewHolder viewHolder;

        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.custom_contact_list_element, parent, false);
        viewHolder.weared_from = convertView.findViewById(R.id.custom_view_date_weared_from);
        viewHolder.weared_to = convertView.findViewById(R.id.custom_view_date_weared_to);
        viewHolder.weared_during = convertView.findViewById(R.id.custom_view_date_time_weared);

        viewHolder.weared_from.setText(dataModel.getDatePut());
        viewHolder.weared_to.setText(dataModel.getDateRemoved());
        viewHolder.weared_during.setText(convertTimeWeared(dataModel.getTimeWeared()));

        return convertView;
    }
}
