package com.lubenard.oring_reminder.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.CustomListAdapter;
import com.lubenard.oring_reminder.custom_components.CustomSpermoListAdapter;
import com.lubenard.oring_reminder.custom_components.Spermograms;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class MySpermogramsFragment extends Fragment implements CustomListAdapter.onListItemClickListener{
    private static RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private static ArrayList<Spermograms> dataModels;
    private static DbManager dbManager;
    private static CustomSpermoListAdapter adapter;
    private static CustomListAdapter.onListItemClickListener onListItemClickListener;


    private final static String TAG = "MySpermograms";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.my_spermograms_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.my_spermo_title_fragment);

        recyclerView = view.findViewById(R.id.spermo_list);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        dataModels = new ArrayList<>();
        dbManager = MainActivity.getDbManager();

    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
        dataModels.clear();
        LinkedHashMap<Integer, Spermograms> entrysDatas = dbManager.getAllSpermograms();
        for (LinkedHashMap.Entry<Integer, Spermograms> oneElemData : entrysDatas.entrySet())
            dataModels.add(oneElemData.getValue());
        adapter = new CustomSpermoListAdapter(dataModels, onListItemClickListener);
        recyclerView.setAdapter(adapter);
    }

    /**
     * onClickManager handling clicks on the main List
     */
    @Override
    public void onListItemClickListener(int position) {
        Spermograms dataModel= dataModels.get(position);
        Log.d(TAG, "Element " + dataModel.getId());
        /*EntryDetailsFragment fragment = new EntryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("entryId", dataModel.getId());
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, null)
                .addToBackStack(null).commit();*/
    }

}
