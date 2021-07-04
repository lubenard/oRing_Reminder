package com.lubenard.oring_reminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.Spermograms;

public class SpermogramsDetailsFragment extends Fragment {

    private long entryId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.spermograms_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // To change ?
        getActivity().setTitle(R.string.action_my_spermograms);

        Bundle bundle = this.getArguments();
        entryId = bundle.getLong("entryId", -1);

        PDFView pdfView = view.findViewById(R.id.spermo_pdf_viewer);
        DbManager dbManager = MainActivity.getDbManager();

        Spermograms datas = dbManager.getSpermoEntryForId(entryId);

        pdfView.fromUri(datas.getFileAddr()).load();
    }
}
