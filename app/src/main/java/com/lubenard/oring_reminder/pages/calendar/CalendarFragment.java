package com.lubenard.oring_reminder.pages.calendar;

import static androidx.core.content.ContextCompat.getDrawable;

import android.os.Bundle;

import com.lubenard.oring_reminder.pages.history.HistoryFragment;
import com.lubenard.oring_reminder.utils.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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

public class CalendarFragment extends Fragment {

    private final static String TAG = "CalendarFragment";

    private RecyclerView calendarRecyclerView;
    private CalendarAdapter adapter;
    private DbManager dbManager;
    private static FragmentActivity activity;

    private LinearLayoutManager linearLayoutManager;

    private final MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_calendar, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            int id = menuItem.getItemId();
            if (id == R.id.action_view_as_history) {
                activity.removeMenuProvider(menuProvider);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new HistoryFragment(), null)
                        .addToBackStack(null).commit();
                return true;
            }
            return false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.calendar_fragment, container, false);

        Log.d(TAG, "onCreateView()");
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(R.string.calendar_fragment_title);

        Log.d(TAG, "onViewCreated()");

        activity = requireActivity();

        ((AppCompatActivity)activity).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activity.addMenuProvider(menuProvider);

        calendarRecyclerView = view.findViewById(R.id.calendar_list);
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
            adapter = new CalendarAdapter(activity, this, entries.get(0).getDatePutCalendar());
        else
            adapter = new CalendarAdapter(activity, this, Calendar.getInstance());
        calendarRecyclerView.setAdapter(adapter);

        Log.d(TAG, "calendarRecyclerView has " + calendarRecyclerView.getChildCount() + " childs");
    }

    public void removeMenuProvider() {
        activity.removeMenuProvider(menuProvider);
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView()");
        activity.removeMenuProvider(menuProvider);
        super.onDestroyView();
    }
}
