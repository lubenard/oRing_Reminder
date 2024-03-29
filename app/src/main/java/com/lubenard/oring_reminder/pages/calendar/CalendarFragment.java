package com.lubenard.oring_reminder.pages.calendar;

import static androidx.core.content.ContextCompat.getDrawable;

import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.pages.history.HistoryFragment;
import com.lubenard.oring_reminder.ui.adapters.CalendarAdapter;
import com.lubenard.oring_reminder.utils.Log;

import java.util.Calendar;

public class CalendarFragment extends Fragment {

    private final static String TAG = "CalendarFragment";

    private RecyclerView calendarRecyclerView;
    private CalendarAdapter adapter;
    private CalendarViewModel calendarViewModel;
    private static FragmentActivity activity;

    private final MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_calendar, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            int id = menuItem.getItemId();
            if (id == R.id.action_view_as_list) {
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
        Log.d(TAG, "onCreateView()");
        return inflater.inflate(R.layout.calendar_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated()");
        activity = requireActivity();
        activity.setTitle(R.string.calendar_fragment_title);
        ((AppCompatActivity)activity).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activity.addMenuProvider(menuProvider);

        calendarRecyclerView = view.findViewById(R.id.calendar_list);
        // Since the recyclerView has fixed size (according to screen size),
        // this is used for optimization
        calendarRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        calendarRecyclerView.setLayoutManager(linearLayoutManager);

        calendarViewModel = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
        calendarViewModel.loadCalendarInfos();

        // Add dividers (like listView) to recyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(requireContext(), R.drawable.empty_tall_divider_calendar));
        calendarRecyclerView.addItemDecoration(dividerItemDecoration);

        calendarViewModel.allSessions.observe(getViewLifecycleOwner(), entries -> {
            if (entries.size() > 0)
                adapter = new CalendarAdapter(activity, this, entries.get(0).getDatePutCalendar());
            else
                adapter = new CalendarAdapter(activity, this, Calendar.getInstance());
            calendarRecyclerView.setAdapter(adapter);
            Log.d(TAG, "calendarRecyclerView has " + calendarRecyclerView.getChildCount() + " children");
        });

    }

    public void removeMenuProvider() {
        activity.removeMenuProvider(menuProvider);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView()");
        activity.removeMenuProvider(menuProvider);
        super.onDestroyView();
    }
}
