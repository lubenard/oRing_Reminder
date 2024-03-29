package com.lubenard.oring_reminder.pages.datas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.utils.DateUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DatasFragment extends Fragment {

    private static final String TAG = "DataFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.datas_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        requireActivity().setTitle(R.string.data_fragment_title);

        DbManager dbManager = MainActivity.getDbManager();

        ArrayList<RingSession> datas = dbManager.getAllDatasForAllEntrys();

        TextView numberOfEntries = view.findViewById(R.id.number_of_entries);
        TextView lastEntry = view.findViewById(R.id.last_entry);
        TextView firstEntry = view.findViewById(R.id.first_entry);
        TextView timeBetweenFirstAndLastEntry = view.findViewById(R.id.converted_time_between_first_and_last_entries);

        numberOfEntries.setText(String.format(getString(R.string.number_of_entries), datas.size()));
        String lastEntryData;
        String firstEntryData;
        String timeBetweenLastAndFirstData;

        if (datas.size() > 0) {
            lastEntryData = datas.get(datas.size() - 1).getStartDate().split(" ")[0];
            firstEntryData = datas.get(0).getEndDate().split(" ")[0];

            int seconds = (int) DateUtils.getDateDiff(datas.get(0).getStartDate(), datas.get(datas.size() - 1).getStartDate(), TimeUnit.SECONDS);
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

        lastEntry.setText(String.format(getString(R.string.last_entry), lastEntryData));
        firstEntry.setText(String.format(getString(R.string.first_entry), firstEntryData));
        timeBetweenFirstAndLastEntry.setText(timeBetweenLastAndFirstData);
    }
}
