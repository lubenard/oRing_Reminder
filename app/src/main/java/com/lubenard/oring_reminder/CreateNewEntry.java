package com.lubenard.oring_reminder;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateNewEntry extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_entry);

        EditText new_entry_date_from = findViewById(R.id.new_entry_date_from);
        EditText new_entry_time_from = findViewById(R.id.new_entry_time_from);

        EditText new_entry_date_to = findViewById(R.id.new_entry_date_to);
        EditText new_entry_time_to = findViewById(R.id.new_entry_time_to);

        Button auto_from_button = findViewById(R.id.new_entry_auto_date_from);
        Button new_entry_auto_date_to = findViewById(R.id.new_entry_auto_date_to);

        auto_from_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String[] fullDate = dateFormat.format(date).split(" ");
                new_entry_date_from.setText(fullDate[0]);
                new_entry_time_from.setText(fullDate[1]);
            }
        });

        new_entry_auto_date_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String[] fullDate = dateFormat.format(date).split(" ");
                new_entry_date_to.setText(fullDate[0]);
                new_entry_time_to.setText(fullDate[1]);
            }
        });
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

            String formattedDatePut = String.format("%s %s", datePut, timePut);
            String formattedDateRemoved = String.format("%s %s", dateRemoved, timeRemoved);

            Log.d("Create new entry", "Formatted string Put is = " + formattedDatePut);
            Log.d("Create new entry", "Formatted string removed is = " + formattedDateRemoved);

            dbManager.createNewDatesRing(formattedDatePut, formattedDateRemoved);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
