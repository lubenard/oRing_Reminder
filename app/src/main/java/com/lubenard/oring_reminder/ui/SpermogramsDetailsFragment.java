package com.lubenard.oring_reminder.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;

import com.github.barteksc.pdfviewer.PDFView;
import com.lubenard.oring_reminder.BackupRestore;
import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.Spermograms;
import com.lubenard.oring_reminder.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SpermogramsDetailsFragment extends Fragment {

    private long entryId;
    private DbManager dbManager;
    private FragmentManager fragmentManager;
    private TextView date;

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
        date = view.findViewById(R.id.spermo_pdf_date);

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

    /**
     * Check if the input string is valid
     * @param text the given input string
     * @return 1 if the string is valid, else 0
     */
    public static boolean checkDateInputSanity(String text) {
        if (text.equals(""))
            return false;
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(text);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case  R.id.action_edit_entry:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.alertdialog_edit_spermo_title);
                final View customLayout = getLayoutInflater().inflate(R.layout.alertdialog_edit_spermo, null);
                EditText textview_newdate = customLayout.findViewById(R.id.spermogram_new_date);
                ImageButton new_date_picker = customLayout.findViewById(R.id.new_date_datepicker);
                new_date_picker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar c = Calendar.getInstance();
                        int mYear = c.get(Calendar.YEAR);
                        int mMonth = c.get(Calendar.MONTH);
                        int mDay = c.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                                (view, year, monthOfYear, dayOfMonth) -> textview_newdate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth), mYear, mMonth, mDay);
                        datePickerDialog.show();
                    }
                });
                builder.setView(customLayout);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkDateInputSanity(textview_newdate.getText().toString())) {
                            dbManager.updateSpermogram(entryId, textview_newdate.getText().toString());
                            // Used to refresh the date
                            date.setText(getContext().getString(R.string.added_the) + textview_newdate.getText().toString());
                        }
                        else
                            Toast.makeText(getContext(),"The date is not correct, please fix it", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,null);
                AlertDialog dialog_edit = builder.create();
                dialog_edit.show();
                return true;
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
