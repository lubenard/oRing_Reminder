package com.lubenard.oring_reminder;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;

public class CreateNewEntry extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_entry);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        DbManager dbManager = new DbManager(this);

        if (id == R.id.action_validate) {
            String datePut = ((EditText)findViewById(R.id.new_entry_date_from)).getText().toString();
            String timePut = ((EditText)findViewById(R.id.new_entry_time_from)).getText().toString();

            String dateRemoved = ((EditText)findViewById(R.id.new_entry_date_to)).getText().toString();
            String timeRemoved = ((EditText)findViewById(R.id.new_entry_time_to)).getText().toString();


            String formattedDatePut = String.format("%s %s:00", datePut, timePut);

            String formattedDateRemoved = String.format("%s %s:00", dateRemoved, timeRemoved);

            Log.d("Create new entry", "Formatted string Put is = " + formattedDatePut);
            Log.d("Create new entry", "Formatted string removed is = " + formattedDateRemoved);

            dbManager.createNewDatesRing(formattedDatePut, formattedDateRemoved);

            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
