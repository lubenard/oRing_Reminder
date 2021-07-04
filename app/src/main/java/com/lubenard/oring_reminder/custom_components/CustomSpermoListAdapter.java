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

import java.util.ArrayList;

public class CustomSpermoListAdapter extends RecyclerView.Adapter<SpermoListViewHolder> {

    private ArrayList<Spermograms> entryList;
    private static DbManager dbManager;
    private Context context;
    private CustomListAdapter.onListItemClickListener onListItemClickListener;

    public CustomSpermoListAdapter(ArrayList<Spermograms> datas, CustomListAdapter.onListItemClickListener onListItemClickListener) {
        entryList = datas;
        this.onListItemClickListener = onListItemClickListener;
    }

    @NonNull
    @Override
    public SpermoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.custom_spermo_list_element, parent, false);
        dbManager = MainActivity.getDbManager();
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
