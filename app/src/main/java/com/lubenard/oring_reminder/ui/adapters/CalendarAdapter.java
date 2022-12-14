package com.lubenard.oring_reminder.ui.adapters;

import android.app.Activity;
import android.content.Context;

import com.lubenard.oring_reminder.ui.fragments.CalendarFragment;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.ui.viewHolders.CalendarViewHolder;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList <Calendar> monthList;
    private Context context;
    private final FragmentActivity activity;
    private CalendarFragment calendarFragment;

    public CalendarAdapter(FragmentActivity activity, CalendarFragment calendarFragment, Calendar firstSession) {

        Log.d("CalendarItemAdapter", "firstSession say is " + DateUtils.getdateFormatted(firstSession.getTime()));

        monthList = new ArrayList<>();

        this.activity = activity;
        this.calendarFragment = calendarFragment;

        Calendar todayDate = Calendar.getInstance();

        int diffYear = todayDate.get(Calendar.YEAR) - firstSession.get(Calendar.YEAR);
        Log.d("CalendarItemAdapter", "diffYear is " + diffYear);
        int diffMonth = diffYear * 12 + todayDate.get(Calendar.MONTH) - firstSession.get(Calendar.MONTH);

        diffMonth++;

        Log.d("CalendarItemAdapter", "diffMonth is " + diffMonth);

        int monthDiffCounter = firstSession.get(Calendar.MONTH);
        int yearCounter = firstSession.get(Calendar.YEAR);

        Log.d("CalendarItemAdapter", "Before loop: " + monthDiffCounter + "/" + yearCounter);

        for (int i = 0; i < diffMonth; i++) {
            if (monthDiffCounter % 12 == 0) {
                Log.d("CalendarItemAdapter", "Increasing year counter");
                monthDiffCounter = 0;
                yearCounter++;
            }
            Log.d("CalendarItemAdapter", "Creating item with datas " + monthDiffCounter + "/" + yearCounter);
            Calendar calendarItem = Calendar.getInstance();
            calendarItem.set(yearCounter, monthDiffCounter, 1);
            monthList.add(calendarItem);
            monthDiffCounter++;
        }

        Log.d("CalendarItemAdapter", "CalendarItemAdapter is initialised");
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_item, parent, false);
        context = parent.getContext();
        Log.d("CalendarItemAdapter", "CalendarItemAdapter: returning ViewHolder");
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
