package com.lubenard.oring_reminder.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.ui.viewHolders.CalendarViewHolder;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private ArrayList <Calendar> monthList;
    private Context context;
    private onListItemClickListener onListItemClickListener;

    public CalendarAdapter(Calendar firstSession, Calendar lastSession, onListItemClickListener onListItemClickListener) {

        monthList = new ArrayList<>();

        int diffYear = lastSession.get(Calendar.YEAR) - firstSession.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + lastSession.get(Calendar.MONTH) - firstSession.get(Calendar.MONTH);

        if (diffMonth == 0)
            diffMonth = 1;

        Log.d("CalendarItemAdapter", "diffMonth is " + diffMonth);

        int monthDiffCounter = firstSession.get(Calendar.MONTH);
        int yearCounter = firstSession.get(Calendar.YEAR);
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

        this.onListItemClickListener = onListItemClickListener;
        Log.d("CalendarItemAdapter", "CalendarItemAdapter is initialised");
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_item, parent, false);
        context = parent.getContext();
        Log.d("CalendarItemAdapter", "CalendarItemAdapter: returning ViewHolder");
        return new CalendarViewHolder(view, onListItemClickListener, context);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.updateDatas(monthList.get(position), context);
    }

    @Override
    public int getItemCount() {
        return monthList.size();
    }

    public interface onListItemClickListener {
        void onListItemClickListener(int position);
    }
}
