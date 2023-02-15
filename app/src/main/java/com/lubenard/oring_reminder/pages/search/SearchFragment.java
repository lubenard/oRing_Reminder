package com.lubenard.oring_reminder.pages.search;

import android.os.Bundle;

import com.lubenard.oring_reminder.pages.entry_details.EntryDetailsFragment;
import com.lubenard.oring_reminder.utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.ui.adapters.ListSearchAdapter;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private ListSearchAdapter adapter;
    private ArrayList<RingSession> dataModels;
    private ListView listView;

    public static final String TAG = "SearchFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreateView()");
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    /**
     * Update Result list with result search
     * @param pausesDatas
     */
    private void updateResultList(ArrayList<RingSession> pausesDatas) {
        dataModels.clear();
        dataModels.addAll(pausesDatas);
        adapter = new ListSearchAdapter(dataModels, getContext());
        listView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DbManager dbManager = MainActivity.getDbManager();

        Bundle bundle = this.getArguments();

        String date_searched = bundle.getString("date_searched", "NOTHING");

        Log.d(TAG, "date is " + date_searched);

        ArrayList<RingSession> results = dbManager.searchEntryInDb(date_searched);

        TextView search_result = view.findViewById(R.id.no_result_found_search);

        listView = view.findViewById(R.id.result_search_listview);

        if (results.size() > 0) {
            getActivity().setTitle(String.format("%s: %d", getString(R.string.search_result), results.size()));
            search_result.setVisibility(View.GONE);
        } else {
            getActivity().setTitle(R.string.search_result);
            search_result.setText(R.string.no_entry_found_for_date);
            listView.setVisibility(View.GONE);
        }

        dataModels = new ArrayList<>();

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            RingSession dataModel = dataModels.get(position);
            Log.d(TAG, "Element " + dataModel.getId());
            EntryDetailsFragment fragment = new EntryDetailsFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putLong("entryId", dataModel.getId());
            fragment.setArguments(bundle1);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment, null)
                    .addToBackStack(null).commit();
        });

        updateResultList(results);
    }
}
