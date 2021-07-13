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

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

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

    public void updateElementDatas(Spermograms dataModel, Context context) {
        dateAdded.setText(context.getString(R.string.added_the) + dataModel.getDateAdded());
        Log.d("Pdf View", "Loaded date " + dataModel.getDateAdded() + " path: " + dataModel.getFileAddr());
        generateImageFromPdf(dataModel.getFileAddr(), context);
    }

    // Code for this function has been found here
    // https://stackoverflow.com/questions/38828396/generate-thumbnail-of-pdf-in-android
    void generateImageFromPdf(Uri pdfUri, Context context) {
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        try {
            //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(pdfUri, "r");
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            Bitmap bmp = Bitmap.createBitmap(width, (height / 100) * 75, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
            pdfView.setImageBitmap(bmp);
            pdfiumCore.closeDocument(pdfDocument); // important!
        } catch(Exception e) {
            //todo with exception
        }
    }

    @Override
    public void onClick(View view) {
        onListItemClickListener.onListItemClickListener(getAdapterPosition());
    }
}
