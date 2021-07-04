package com.lubenard.oring_reminder.custom_components;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpermoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context context;
    private CustomListAdapter.onListItemClickListener onListItemClickListener;

    public SpermoViewHolder(@NonNull View itemView, CustomListAdapter.onListItemClickListener onListItemClickListener) {
        super(itemView);

        this.onListItemClickListener = onListItemClickListener;
        itemView.setOnClickListener(this);
    }

    public void updateElementDatas(Spermograms dataModel, Context context) {

    }

    @Override
    public void onClick(View view) {
        onListItemClickListener.onListItemClickListener(getAdapterPosition());
    }
}
