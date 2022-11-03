package com.lubenard.oring_reminder.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.ui.viewHolders.CalendarItemViewHolder;

import java.util.ArrayList;

public class CalendarItemAdapter extends RecyclerView.Adapter<CalendarItemViewHolder> {

    private ArrayList <String> monthList;
    private Context context;
    private onListItemClickListener onListItemClickListener;

    public CalendarItemAdapter(ArrayList<String> datas, onListItemClickListener onListItemClickListener) {
        monthList = datas;
        this.onListItemClickListener = onListItemClickListener;
        Log.d("CalendarItemAdapter", "CalendarItemAdapter is initialised");
    }

    @NonNull
    @Override
    public CalendarItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_item, parent, false);
        context = parent.getContext();
        Log.d("CalendarItemAdapter", "CalendarItemAdapter: returning ViewHolder");
        return new CalendarItemViewHolder(view, onListItemClickListener, context);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarItemViewHolder holder, int position) {
        holder.updateDatas(monthList.get(position), context);
    }

    @Override
    public int getItemCount() {
        return monthList.size();
    }

    public interface onListItemClickListener {
        void onListItemClickListener(int position);
    }
}
