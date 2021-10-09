package com.lubenard.oring_reminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DatasFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.datas_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.data_fragment_title);

        DbManager dbManager = MainActivity.getDbManager();

        ArrayList<RingSession> datas = dbManager.getAllDatasForAllEntrys();

        TextView numberOfEntries = view.findViewById(R.id.number_of_entries);
        TextView lastEntry = view.findViewById(R.id.last_entry);
        TextView firstEntry = view.findViewById(R.id.first_entry);
        TextView timeBetweenLastAndFirst = view.findViewById(R.id.time_between_first_and_last_entries);


        numberOfEntries.setText(getString(R.string.number_of_entries)+ datas.size());
        String lastEntryData;
        String firstEntryData;
        String timeBetweenLastAndFirstData;

        if (datas.size() > 0) {
            lastEntryData = datas.get(datas.size() - 1).getDatePut().split(" ")[0];
            firstEntryData = datas.get(0).getDatePut().split(" ")[0];
            timeBetweenLastAndFirstData = String.valueOf(Utils.getDateDiff(datas.get(0).getDatePut(), datas.get(datas.size() - 1).getDatePut(), TimeUnit.DAYS) + 1);
        } else {
            lastEntryData = getString(R.string.not_set_yet);
            firstEntryData = getString(R.string.not_set_yet);
            timeBetweenLastAndFirstData = getString(R.string.not_set_yet);
        }

        lastEntry.setText(getString(R.string.last_entry) + "\n" + lastEntryData);
        firstEntry.setText(getString(R.string.first_entry) + "\n" + firstEntryData);
        timeBetweenLastAndFirst.setText(getString(R.string.number_of_days_between_entries) + "\n" + timeBetweenLastAndFirstData);
    }
}
