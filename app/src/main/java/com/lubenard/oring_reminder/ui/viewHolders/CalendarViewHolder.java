package com.lubenard.oring_reminder.ui.viewHolders;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.ui.adapters.CalendarAdapter;
import com.lubenard.oring_reminder.ui.adapters.CalendarItemAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private CalendarAdapter.onListItemClickListener onListItemClickListener;
    private CalendarItemAdapter.onListItemClickListener onGridItemClickListener;
    private GridView calendarGridDays;
    private TextView calendarMonth;

    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.onListItemClickListener onListItemClickListener, Context context) {
        super(itemView);

        calendarGridDays = itemView.findViewById(R.id.calendarGridDays);
        calendarMonth = itemView.findViewById(R.id.calendarMonth);
        this.onListItemClickListener = onListItemClickListener;
        itemView.setOnClickListener(this);
    }

    /**
     * Update the history element
     * @param date date to base data on
     * @param context context
     */
    public void updateDatas(Calendar date, Context context) {
        Log.d("CalendarViewHolder", "Received date" + date);

        calendarMonth.setText(String.format("%s %d", date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), date.get(Calendar.YEAR)));

        ArrayList<String> num = listOfDatesInMonth(date);

        int calendarOffset = getIndexOfFirstDayInMonth(date);

        final CalendarItemAdapter adapter = new CalendarItemAdapter(context, num, calendarOffset, onGridItemClickListener);

        calendarGridDays.setAdapter(adapter);
    }

    ArrayList<String> listOfDatesInMonth(Calendar selectedMonthFirstDay) {
        Calendar nextMonthFirstDay = Calendar.getInstance();
        nextMonthFirstDay.set(selectedMonthFirstDay.get(Calendar.YEAR),
                selectedMonthFirstDay.get(Calendar.DAY_OF_MONTH) + 1, selectedMonthFirstDay.get(Calendar.DAY_OF_MONTH));

        Log.d("CalendarItemViewHolder", "listOfDatesInMonth: nextMonthFirstDay is " + selectedMonthFirstDay.getActualMaximum(Calendar.DAY_OF_MONTH));

        ArrayList<String> list = new ArrayList<>();

        for (int i = 1; i < selectedMonthFirstDay.getActualMaximum(Calendar.DAY_OF_MONTH) + 1; i++) {
            list.add(String.valueOf(i));
        }

        return (list);
    }

    int getIndexOfFirstDayInMonth(Calendar currentDate) {
        Log.d("CalendarItemViewHolder", "currentDate: " + currentDate);

        String[] daysOfWeek = { "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

        String day = new SimpleDateFormat("EEE").format(currentDate.getTime()).toUpperCase();
        String month = new SimpleDateFormat("MMM").format(currentDate.getTime()).toUpperCase();
        return Arrays.asList(daysOfWeek).indexOf(day);
    }

    @Override
    public void onClick(View view) {
        //onListItemClickListener.onListItemClickListener(getAdapterPosition());
    }
}
