package com.lubenard.oring_reminder.custom_components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.ui.MainFragment;

import java.util.ArrayList;

public class CustomListAdapter extends RecyclerView.Adapter<MainListViewHolder> {

    private ArrayList <RingModel> entryList;
    private Context context;
    private onListItemClickListener onListItemClickListener;

    public CustomListAdapter(ArrayList<RingModel> datas, onListItemClickListener onListItemClickListener) {
        entryList = datas;
        this.onListItemClickListener = onListItemClickListener;
    }

    @NonNull
    @Override
    public MainListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.custom_entry_list_element, parent, false);
        context = parent.getContext();
        return new MainListViewHolder(view, onListItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MainListViewHolder holder, int position) {
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
