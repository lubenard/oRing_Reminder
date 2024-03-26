package com.lubenard.oring_reminder.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.pages.calendar.CalendarFragment;
import com.lubenard.oring_reminder.ui.viewHolders.CalendarViewHolder;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private static final String TAG = "CalendarAdapter";
    private final ArrayList <Calendar> monthList;
    private Context context;
    private final FragmentActivity activity;
    private final CalendarFragment calendarFragment;

    public CalendarAdapter(FragmentActivity activity, CalendarFragment calendarFragment, Calendar firstSession) {

        Log.d(TAG, "firstSession say is " + DateUtils.Companion.getdateFormatted(firstSession.getTime()));

        monthList = new ArrayList<>();

        this.activity = activity;
        this.calendarFragment = calendarFragment;

        Calendar todayDate = Calendar.getInstance();

        int diffYear = todayDate.get(Calendar.YEAR) - firstSession.get(Calendar.YEAR);
        Log.d(TAG, "diffYear is " + diffYear);
        int diffMonth = (diffYear * 12 + todayDate.get(Calendar.MONTH) - firstSession.get(Calendar.MONTH)) + 1;

        Log.d(TAG, "diffMonth is " + diffMonth);

        int monthDiffCounter = firstSession.get(Calendar.MONTH);
        int yearCounter = firstSession.get(Calendar.YEAR);

        Log.d(TAG, "Before loop: " + monthDiffCounter + "/" + yearCounter);

        for (int i = 0; i < diffMonth; i++) {
            if (monthDiffCounter % 12 == 0 && i != 0) {
                Log.d(TAG, "Increasing year counter");
                monthDiffCounter = 0;
                yearCounter++;
            }
            Log.d(TAG, "Creating calendarItem with datas " + (monthDiffCounter + 1) + "/" + yearCounter);
            Calendar calendarItem = Calendar.getInstance();
            calendarItem.set(yearCounter, monthDiffCounter, 1);
            monthList.add(calendarItem);
            monthDiffCounter++;
        }

        Log.d(TAG, "CalendarItemAdapter is initialised");
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_item, parent, false);
        context = parent.getContext();
        Log.d(TAG, "CalendarItemAdapter: returning ViewHolder");
        return new CalendarViewHolder(view, activity, context, calendarFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        DbManager dbManager = MainActivity.getDbManager();
        holder.updateDatas(monthList.get(position), dbManager.getEntriesForMonth(monthList.get(position)), context);
    }

    @Override
    public int getItemCount() {
        return monthList.size();
    }
}
