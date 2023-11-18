package com.lubenard.oring_reminder.ui.viewHolders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.custom_components.Session;
import com.lubenard.oring_reminder.ui.adapters.HistoryListAdapter;
import com.lubenard.oring_reminder.utils.DateUtils;

import java.util.Calendar;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView weared_from;
    private TextView weared_to;
    private TextView weared_during;
    private TextView worn_date;
    private Context context;
    private HistoryListAdapter.onListItemClickListener onListItemClickListener;

    public HistoryViewHolder(@NonNull View itemView, HistoryListAdapter.onListItemClickListener onListItemClickListener, Context context) {
        super(itemView);
        worn_date = itemView.findViewById(R.id.main_history_date);
        weared_from = itemView.findViewById(R.id.history_listview_item_hour_from);
        weared_to = itemView.findViewById(R.id.history_listview_item_hour_to);
        weared_during = itemView.findViewById(R.id.history_listview_item_time_weared);
        this.onListItemClickListener = onListItemClickListener;
        itemView.setOnClickListener(this);
    }

    /**
     * Update the history element
     * @param dataModel fresh datas
     * @param context context
     */
    public void updateElementDatas(RingSession dataModel, Context context) {
        this.context = context;

        String[] datePut = dataModel.getStartDate().split(" ");

        weared_from.setText(datePut[1]);

        if (dataModel.getStatus() == Session.SessionStatus.RUNNING)
            worn_date.setText(DateUtils.convertDateIntoReadable(dataModel.getDatePutCalendar(), false));
        else if (dataModel.getDatePutCalendar().get(Calendar.YEAR) == dataModel.getDateRemovedCalendar().get(Calendar.YEAR) &&
            dataModel.getDatePutCalendar().get(Calendar.MONTH) == dataModel.getDateRemovedCalendar().get(Calendar.MONTH) &&
            dataModel.getDatePutCalendar().get(Calendar.DAY_OF_MONTH) == dataModel.getDateRemovedCalendar().get(Calendar.DAY_OF_MONTH))
            worn_date.setText(DateUtils.convertDateIntoReadable(dataModel.getDatePutCalendar(), false));
        else if (!(dataModel.getStatus() == Session.SessionStatus.RUNNING))
            worn_date.setText(DateUtils.convertDateIntoReadable(dataModel.getDatePutCalendar(), false) + " -> " + DateUtils.convertDateIntoReadable(dataModel.getEndDate().split(" ")[0], false));

        if (dataModel.getStatus() == Session.SessionStatus.RUNNING) {
            weared_to.setText("");
            long timeBeforeRemove = dataModel.getSessionDuration() - dataModel.computeTotalTimePause();
            weared_during.setTextColor(context.getResources().getColor(R.color.yellow));
            weared_during.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
        } else {
            String[] dateRemoved = dataModel.getEndDate().split(" ");
            weared_to.setText(dateRemoved[1]);

            long totalTimePause = dataModel.getSessionDuration();
            if (totalTimePause >= MainActivity.getSettingsManager().getWearingTimeInt())
                weared_during.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                weared_during.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            weared_during.setText(DateUtils.convertIntIntoReadableDate((int)totalTimePause));
        }
    }

    @Override
    public void onClick(View view) {
        onListItemClickListener.onListItemClickListener(getAdapterPosition());
    }
}
