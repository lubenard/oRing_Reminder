package com.lubenard.oring_reminder.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.ui.adapters.CalendarItemAdapter;

import java.util.ArrayList;

public class CalendarFragment extends Fragment implements CalendarItemAdapter.onListItemClickListener {

    RecyclerView calendarRecyclerView;
    CalendarItemAdapter adapter;

    private CalendarItemAdapter.onListItemClickListener onListItemClickListener;
    private LinearLayoutManager linearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.calendar_fragment, container, false);

        onListItemClickListener = this;

        ArrayList<String> dataModels = new ArrayList<>();

        dataModels.add("january");
        dataModels.add("february");
        dataModels.add("mars");

        Log.d("CalendarFragment", "Should be Launching ItemAdapter, having " + dataModels.size() + " elements");

        calendarRecyclerView = view.findViewById(R.id.calendar_list);


        // Since the recyclerView has fixed size (according to screen size),
        // this is used for optimization
        calendarRecyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());
        calendarRecyclerView.setLayoutManager(linearLayoutManager);

        adapter = new CalendarItemAdapter(dataModels, onListItemClickListener);
        calendarRecyclerView.setAdapter(adapter);

        Log.d("CalendarFragment", "calendarRecyclerView has " + calendarRecyclerView.getChildCount());

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.calendar_fragment_title);
        Log.d("CalendarFragment", "View is created");
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * onClickManager handling clicks on the main List
     */
    @Override
    public void onListItemClickListener(int position) {

    }
}
