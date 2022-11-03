package com.lubenard.oring_reminder.ui.viewHolders;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.ui.adapters.CalendarItemAdapter;
import com.lubenard.oring_reminder.ui.adapters.HistoryListAdapter;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CalendarItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private CalendarItemAdapter.onListItemClickListener onListItemClickListener;
    private GridView calendarGridDays;
    private TextView calendarMonth;

    public CalendarItemViewHolder(@NonNull View itemView, CalendarItemAdapter.onListItemClickListener onListItemClickListener, Context context) {
        super(itemView);

        calendarGridDays = itemView.findViewById(R.id.calendarGridDays);
        calendarMonth = itemView.findViewById(R.id.calendarMonth);
        this.onListItemClickListener = onListItemClickListener;
        itemView.setOnClickListener(this);
    }

    /**
     * Update the history element
     * @param month fresh datas
     * @param context context
     */
    public void updateDatas(String month, Context context) {
        Log.d("CalendarItemViewHolder", "Received month" + month);

        ArrayList<String> num = new ArrayList<>();

        for (int i = 0; i != 32 ; i++ ) {
            num.add(String.valueOf(i));
        }

        calendarMonth.setText("Janary 2022");

        final ArrayAdapter adapter = new ArrayAdapter(context, R.layout.calendar_grid_item, R.id.calendar_grid_item_layout, num);

        calendarGridDays.setAdapter(adapter);

    }

    @Override
    public void onClick(View view) {
        onListItemClickListener.onListItemClickListener(getAdapterPosition());
    }
}
