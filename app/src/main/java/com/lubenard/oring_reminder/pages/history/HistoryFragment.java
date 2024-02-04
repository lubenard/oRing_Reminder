package com.lubenard.oring_reminder.pages.history;

import static androidx.core.content.ContextCompat.getDrawable;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.pages.entry_details.EntryDetailsFragment;
import com.lubenard.oring_reminder.ui.adapters.HistoryListAdapter;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class HistoryFragment extends Fragment implements HistoryListAdapter.onListItemClickListener{

    private static final String TAG = "HistoryFragment";

    // We can set thoses variables as static, because we know the view is going to be created
    private static ArrayList<RingSession> dataModels;
    private static DbManager dbManager;
    private static HistoryListAdapter adapter;
    private static RecyclerView recyclerView;
    private static boolean orderEntryByDesc = true;
    private LinearLayoutManager linearLayoutManager;
    private static HistoryListAdapter.onListItemClickListener onListItemClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onViewCreated()");
        return inflater.inflate(R.layout.full_history_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.history);
        ((AppCompatActivity)requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Context context = getContext();

        recyclerView = view.findViewById(R.id.main_list);


        // Add dividers (like listView) to recyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(context, R.drawable.empty_tall_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Since the recyclerView has fixed size (according to screen size),
        // this is used for optimization
        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        onListItemClickListener = this;

        dataModels = new ArrayList<>();
        dbManager = MainActivity.getDbManager();
    }

    /**
     * Update the listView by fetching all elements from the db
     */
    public void updateElementList() {
        Log.d(TAG, "Updated history Listview");
        dataModels.clear();
        LinkedHashMap<Integer, RingSession> entrysDatas = dbManager.getAllDatasForMainList(orderEntryByDesc);
        getActivity().setTitle(getString(R.string.history) + " (" + entrysDatas.size() + " " + getString(R.string.entries) + ")");
        for (LinkedHashMap.Entry<Integer, RingSession> oneElemData : entrysDatas.entrySet())
            dataModels.add(oneElemData.getValue());
        adapter = new HistoryListAdapter(dataModels, onListItemClickListener);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
        updateElementList();
    }

    /**
     * onClickManager handling clicks on the main List
     */
    @Override
    public void onListItemClickListener(int position) {
        RingSession dataModel= dataModels.get(position);
        Log.d(TAG, "Element " + dataModel.getId());
        EntryDetailsFragment fragment = new EntryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("entryId", dataModel.getId());
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, null)
                .addToBackStack(null).commit();
    }
}