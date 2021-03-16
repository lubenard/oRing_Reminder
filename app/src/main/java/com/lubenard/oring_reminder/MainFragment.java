package com.lubenard.oring_reminder;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainFragment extends Fragment {

    private ArrayList<RingModel> dataModels;
    private DbManager dbManager;
    private CustomListAdapter adapter;
    private ListView listView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEntry();
            }
        });

        listView = view.findViewById(R.id.main_list);
        Toolbar toolbar = view.findViewById(R.id.main_toolbar);

        dataModels = new ArrayList<>();

        dbManager = new DbManager(getContext());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RingModel dataModel= dataModels.get(i);
                Log.d("ONCLICK", "Element " + dataModel.getId());
                EntryDetailsFragment fragment = new EntryDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("entryId", dataModel.getId());
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, fragment, null)
                        .addToBackStack(null).commit();
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    // Navigate to settings screen
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new SettingsFragment(), null)
                            .addToBackStack(null).commit();
                    return true;
                default:
                    return false;
            }
        });
    }

    /**
     * Update the listView by fetching all elements from the db
     */
    private void updateElementList() {
        LinkedHashMap<Integer, RingModel> contactsdatas = dbManager.getAllDatasForMainList();
        for (LinkedHashMap.Entry<Integer, RingModel> oneElemDatas : contactsdatas.entrySet()) {
            dataModels.add(oneElemDatas.getValue());
        }
        adapter = new CustomListAdapter(dataModels, getContext());
        listView.setAdapter(adapter);
    }

    /**
     * Launch the new Entry fragment, and specify we do not want to update a entry
     */
    private void createNewEntry() {
        EditEntryFragment fragment = new EditEntryFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("entryId", -1);
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, null)
                .addToBackStack(null).commit();
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
        dataModels.clear();
        updateElementList();
    }
}