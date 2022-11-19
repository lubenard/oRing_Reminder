package com.lubenard.oring_reminder.ui.viewHolders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.ui.adapters.HistoryListAdapter;
import com.lubenard.oring_reminder.utils.Utils;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView weared_from;
    private TextView weared_to;
    private TextView weared_during;
    private TextView worn_date;
    private Context context;
    private View itemView;
    private HistoryListAdapter.onListItemClickListener onListItemClickListener;

    public HistoryViewHolder(@NonNull View itemView, HistoryListAdapter.onListItemClickListener onListItemClickListener, Context context) {
        super(itemView);
        this.itemView = itemView;
        worn_date = itemView.findViewById(R.id.main_history_date);
        weared_from = itemView.findViewById(R.id.custom_view_date_weared_from);
        weared_to = itemView.findViewById(R.id.custom_view_date_weared_to);
        weared_during = itemView.findViewById(R.id.custom_view_date_time_weared);
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

        String[] datePut = dataModel.getDatePut().split(" ");

        if (dataModel.getDatePut().split(" ")[0].equals(dataModel.getDateRemoved().split(" ")[0]))
            worn_date.setText(Utils.convertDateIntoReadable(dataModel.getDatePut().split(" ")[0], false));
        else if (!dataModel.getIsRunning())
            worn_date.setText(Utils.convertDateIntoReadable(dataModel.getDatePut().split(" ")[0], false) + " -> " + Utils.convertDateIntoReadable(dataModel.getDateRemoved().split(" ")[0], false));

        weared_from.setText(datePut[1]);

        if (!dataModel.getDateRemoved().equals("NOT SET YET")) {
            String[] dateRemoved = dataModel.getDateRemoved().split(" ");
            weared_to.setText(dateRemoved[1]);
        } else
            weared_to.setText(dataModel.getDateRemoved());

        if (!dataModel.getIsRunning()) {
            int totalTimePause = SessionsManager.getWearingTimeWithoutPause(dataModel.getDatePut(), dataModel.getId(), dataModel.getDateRemoved());
            if (totalTimePause / 60 >= 15)
                weared_during.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                weared_during.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            weared_during.setText(Utils.convertTimeWeared(totalTimePause));
        }
        else {
            long timeBeforeRemove = SessionsManager.getWearingTimeWithoutPause(dataModel.getDatePut(), dataModel.getId(), null);
            weared_during.setTextColor(context.getResources().getColor(R.color.yellow));
            weared_during.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
        }
    }

    @Override
    public void onClick(View view) {
        onListItemClickListener.onListItemClickListener(getAdapterPosition());
    }
}
