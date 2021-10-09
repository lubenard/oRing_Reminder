package com.lubenard.oring_reminder.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.CustomListSearchAdapter;
import com.lubenard.oring_reminder.custom_components.RingSession;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private CustomListSearchAdapter adapter;
    private ArrayList<RingSession> dataModels;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    private void updateResultList(ArrayList<RingSession> pausesDatas) {
        dataModels.clear();
        dataModels.addAll(pausesDatas);
        adapter = new CustomListSearchAdapter(dataModels, getContext());
        listView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.search_result);

        DbManager dbManager = MainActivity.getDbManager();

        Bundle bundle = this.getArguments();

        String date_searched = bundle.getString("date_searched", "NOTHING");

        Log.d("Search Fragment", "date is " + date_searched);

        ArrayList<RingSession> results = dbManager.searchEntryInDb(date_searched);

        TextView search_result = view.findViewById(R.id.result_search_textview);

        listView = view.findViewById(R.id.result_search_listview);

        dataModels = new ArrayList<>();

        if (results.size() > 0)
            search_result.setText(getString(R.string.total_search) + " " + results.size());
        else
            search_result.setText("No entry found for this date");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RingSession dataModel= dataModels.get(position);
                Log.d("SearchFragment", "Element " + dataModel.getId());
                EntryDetailsFragment fragment = new EntryDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putLong("entryId", dataModel.getId());
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, fragment, null)
                        .addToBackStack(null).commit();
            }
        });

        updateResultList(results);
    }
}
