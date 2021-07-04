package com.lubenard.oring_reminder.custom_components;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;

import com.github.barteksc.pdfviewer.PDFView;

public class SpermoListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView dateAdded;
    private PDFView pdfView;
    private CustomSpermoListAdapter.onListItemClickListener onListItemClickListener;

    public SpermoListViewHolder(@NonNull View itemView, CustomSpermoListAdapter.onListItemClickListener onListItemClickListener) {
        super(itemView);
        dateAdded = itemView.findViewById(R.id.custom_spermo_date_added);
        pdfView = itemView.findViewById(R.id.custom_spermo_pdf_view);
        this.onListItemClickListener = onListItemClickListener;
        itemView.setOnClickListener(this);
    }

    public void updateElementDatas(Spermograms dataModel, Context context) {
        dateAdded.setText("Added the " + dataModel.getDateAdded());
        pdfView.fromUri(dataModel.getFileAddr())
                .pages(0)
                .defaultPage(0)
                .enableSwipe(false)
                .load();
    }

    @Override
    public void onClick(View view) {
        onListItemClickListener.onListItemClickListener(getAdapterPosition());
    }
}
