package com.lubenard.oring_reminder.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.barteksc.pdfviewer.PDFView;
import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.Spermograms;

public class SpermogramsDetailsFragment extends Fragment {

    private long entryId;
    private DbManager dbManager;
    private FragmentManager fragmentManager;

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

        fragmentManager = getActivity().getSupportFragmentManager();

        PDFView pdfView = view.findViewById(R.id.spermo_pdf_viewer);
        TextView date = view.findViewById(R.id.spermo_pdf_date);

         dbManager = MainActivity.getDbManager();

        Spermograms datas = dbManager.getSpermoEntryForId(entryId);

        date.setText(getContext().getString(R.string.added_the) + datas.getDateAdded());

        pdfView.fromUri(datas.getFileAddr())
                .enableDoubletap(true)
                .load();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.spermo_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete_entry:
                // Warn user then delete entry in the db
                new AlertDialog.Builder(getContext()).setTitle(R.string.alertdialog_delete_entry)
                        .setMessage(R.string.alertdialog_delete_contact_body)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            dbManager.deleteSpermoEntry(entryId);
                            fragmentManager.popBackStackImmediate();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
                return true;
            default:
                return false;
        }
    }

}
