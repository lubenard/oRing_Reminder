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

public class CustomListAdapter extends ArrayAdapter<RingModel> implements View.OnClickListener{

    private ArrayList<RingModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView weared_from;
        TextView weared_to;
        TextView weared_during;
    }

    public CustomListAdapter(ArrayList<RingModel> data, Context context) {
        super(context, R.layout.custom_contact_list_element, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        int position=(Integer) v.getTag();
        Object object= getItem(position);
        RingModel dataModel = (RingModel) object;
    }

    private int lastPosition = -1;

    private String convertTimeWeared(int timeWeared) {
        if (timeWeared < 60) {
            return String.valueOf(timeWeared) + " Minutes";
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
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.custom_contact_list_element, parent, false);
        viewHolder.weared_from = (TextView) convertView.findViewById(R.id.custom_view_date_weared_from);
        viewHolder.weared_to = (TextView) convertView.findViewById(R.id.custom_view_date_weared_to);
        viewHolder.weared_during = (TextView) convertView.findViewById(R.id.custom_view_date_time_weared);

        viewHolder.weared_from.setText(dataModel.getDatePut());
        viewHolder.weared_to.setText(dataModel.getDateRemoved());
        viewHolder.weared_during.setText(convertTimeWeared(dataModel.getTimeWeared()));

        // Return the completed view to render on screen
        return convertView;
    }
}
