package com.lubenard.oring_reminder.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.lubenard.oring_reminder.R;

public class CalendarItem extends View {

    public CalendarItem(Context context) {
        super(context);
        inflate(context, R.layout.calendar_item, null);
    }
}
