package com.lubenard.oring_reminder.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.ui.viewHolders.HistoryViewHolder;
import com.lubenard.oring_reminder.custom_components.RingSession;

import java.util.ArrayList;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    private ArrayList <RingSession> entryList;
    private Context context;
    private onListItemClickListener onListItemClickListener;

    public HistoryListAdapter(ArrayList<RingSession> datas, onListItemClickListener onListItemClickListener) {
        entryList = datas;
        this.onListItemClickListener = onListItemClickListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.main_history_one_elem, parent, false);
        context = parent.getContext();
        return new HistoryViewHolder(view, onListItemClickListener, context);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.updateElementDatas(entryList.get(position), context);
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public interface onListItemClickListener {
        void onListItemClickListener(int position);
    }
}
