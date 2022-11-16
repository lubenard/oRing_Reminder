package com.lubenard.oring_reminder.ui.fragments;

import android.os.Bundle;
import android.util.Log;
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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class DatasFragment extends Fragment {

    private static final String TAG = "DataFragment";

    private DbManager dbManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.datas_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.data_fragment_title);

        dbManager = MainActivity.getDbManager();

        ArrayList<RingSession> datas = dbManager.getAllDatasForAllEntrys();

        TextView numberOfEntries = view.findViewById(R.id.number_of_entries);
        TextView lastEntry = view.findViewById(R.id.last_entry);
        TextView firstEntry = view.findViewById(R.id.first_entry);
        TextView timeBetweenFirstAndLastEntry = view.findViewById(R.id.converted_time_between_first_and_last_entries);

        numberOfEntries.setText(getString(R.string.number_of_entries)+ datas.size());
        String lastEntryData;
        String firstEntryData;
        String timeBetweenLastAndFirstData;

        if (datas.size() > 0) {
            lastEntryData = datas.get(datas.size() - 1).getDatePut().split(" ")[0];
            firstEntryData = datas.get(0).getDatePut().split(" ")[0];

            int seconds = (int)Utils.getDateDiff(datas.get(0).getDatePut(), datas.get(datas.size() - 1).getDatePut(), TimeUnit.SECONDS);
            int weeks = seconds / 604800;
            int days = (seconds % 604800) / 86400;
            int hours = ((seconds % 604800) % 86400) / 3600;
            int minutes = (((seconds % 604800) % 86400) % 3600) / 60;
            timeBetweenLastAndFirstData = String.format(getString(R.string.time_worn_appr), weeks, days, hours, minutes);
        } else {
            lastEntryData = getString(R.string.not_set_yet);
            firstEntryData = getString(R.string.not_set_yet);
            timeBetweenLastAndFirstData = getString(R.string.not_set_yet);
        }

        lastEntry.setText(getString(R.string.last_entry) + "\n" + lastEntryData);
        firstEntry.setText(getString(R.string.first_entry) + "\n" + firstEntryData);
        timeBetweenFirstAndLastEntry.setText(timeBetweenLastAndFirstData);
    }
}
