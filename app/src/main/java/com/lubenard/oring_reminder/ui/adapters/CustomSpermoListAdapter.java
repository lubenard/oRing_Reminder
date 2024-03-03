package com.lubenard.oring_reminder.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.Spermograms;
import com.lubenard.oring_reminder.ui.viewHolders.SpermoListViewHolder;

import java.util.ArrayList;

public class CustomSpermoListAdapter extends RecyclerView.Adapter<SpermoListViewHolder> {

    private final ArrayList<Spermograms> entryList;

    private Context context;
    private onListItemClickListener onListItemClickListener;

    public CustomSpermoListAdapter(ArrayList<Spermograms> datas, onListItemClickListener onListItemClickListener) {
        entryList = datas;
        this.onListItemClickListener = onListItemClickListener;
    }

    @NonNull
    @Override
    public SpermoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.custom_spermo_list_element, parent, false);
        context = parent.getContext();
        return new SpermoListViewHolder(view, onListItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SpermoListViewHolder holder, int position) {
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
