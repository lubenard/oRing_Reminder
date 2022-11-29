package com.lubenard.oring_reminder.ui.fragments;

import static androidx.core.content.ContextCompat.getDrawable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.ui.adapters.CalendarAdapter;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarFragment extends Fragment implements CalendarAdapter.onListItemClickListener {

    private final static String TAG = "CalendarFragment";

    RecyclerView calendarRecyclerView;
    CalendarAdapter adapter;
    DbManager dbManager;

    private CalendarAdapter.onListItemClickListener onListItemClickListener;
    private LinearLayoutManager linearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.calendar_fragment, container, false);

        onListItemClickListener = this;
        calendarRecyclerView = view.findViewById(R.id.calendar_list);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(R.string.calendar_fragment_title);

        Log.d("CalendarFragment", "View is created");

        // Since the recyclerView has fixed size (according to screen size),
        // this is used for optimization
        calendarRecyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());
        calendarRecyclerView.setLayoutManager(linearLayoutManager);

        dbManager = MainActivity.getDbManager();

        ArrayList<RingSession> entries = dbManager.getAllDatasForAllEntrys();

        // Add dividers (like listView) to recyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(requireContext(), R.drawable.empty_tall_divider_calendar));
        calendarRecyclerView.addItemDecoration(dividerItemDecoration);

        if (entries.size() > 0)
            adapter = new CalendarAdapter(entries.get(0).getDatePutCalendar(), onListItemClickListener);
        else
            adapter = new CalendarAdapter(Calendar.getInstance(), onListItemClickListener);
        calendarRecyclerView.setAdapter(adapter);

        Log.d("CalendarFragment", "calendarRecyclerView has " + calendarRecyclerView.getChildCount() + " childs");
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
