package com.lubenard.oring_reminder.custom_components;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;

import com.lubenard.oring_reminder.ui.MySpermogramsFragment;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileNotFoundException;

public class SpermoListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView dateAdded;
    //private PDFView pdfView;
    private ImageView pdfView;
    private CustomSpermoListAdapter.onListItemClickListener onListItemClickListener;

    public SpermoListViewHolder(@NonNull View itemView, CustomSpermoListAdapter.onListItemClickListener onListItemClickListener) {
        super(itemView);
        dateAdded = itemView.findViewById(R.id.custom_spermo_date_added);
        pdfView = itemView.findViewById(R.id.custom_spermo_pdf_view);
        this.onListItemClickListener = onListItemClickListener;
        itemView.setOnClickListener(this);
    }

    /**
     * Update the spermogram fragment list
     * @param dataModel
     * @param context
     */
    public void updateElementDatas(Spermograms dataModel, Context context) {
        dateAdded.setText(context.getString(R.string.added_the) + dataModel.getDateAdded());
        Log.d("Pdf View", "Loaded date " + dataModel.getDateAdded() + " path: " + dataModel.getFileAddr());

        File fileUri = new File(dataModel.getFileAddr() + ".jpg");
        if (!fileUri.exists()) {
            Log.d("PDF View", "Thumbnail does not exist ! for file addr : " + dataModel.getFileAddr().toString().substring(7));
            MySpermogramsFragment.generatePdfThumbnail(context, dataModel.getFileAddr().toString().substring(7));
        }

        pdfView.setImageURI(Uri.parse(dataModel.getFileAddr() + ".jpg"));
    }



    @Override
    public void onClick(View view) {
        onListItemClickListener.onListItemClickListener(getAdapterPosition());
    }
}
