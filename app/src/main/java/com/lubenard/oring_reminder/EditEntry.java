package com.lubenard.oring_reminder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EditEntry extends AppCompatActivity {

    private DbManager dbManager;
    private int entryId;

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

        dbManager = new DbManager(this);

        Intent intent = getIntent();
        entryId = intent.getIntExtra("entryId", -1);

        if (entryId != -1) {
            ArrayList<String> datas = dbManager.getEntryDetails(entryId);
            String[] fullDate = datas.get(0).split(" ");
            new_entry_date_from.setText(fullDate[0]);
            new_entry_time_from.setText(fullDate[1]);

            String[] fullDate2 = datas.get(1).split(" ");
            new_entry_date_to.setText(fullDate2[0]);
            new_entry_time_to.setText(fullDate2[1]);
        }

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

            Log.d("Create new entry", "Is empty ? " + dateRemoved.isEmpty() + " " + timeRemoved.isEmpty());
            if (dateRemoved.isEmpty() && timeRemoved.isEmpty()) {
                Log.d("Create new entry", "Only started wearing it");



                if (entryId != -1)
                    dbManager.updateDatesRing(id, formattedDatePut, "NOT SET YET", 1);
                else
                    dbManager.createNewDatesRing(formattedDatePut, "NOT SET YET", 1);

                finish();

            } else if (Utils.getDateDiff(formattedDatePut, formattedDateRemoved, TimeUnit.MINUTES) > 0) {

                Log.d("Create new entry", "Formatted string Put is = " + formattedDatePut);
                Log.d("Create new entry", "Formatted string removed is = " + formattedDateRemoved);

                if (entryId != -1)
                    dbManager.updateDatesRing(id, formattedDatePut, formattedDateRemoved, 0);
                else
                    dbManager.createNewDatesRing(formattedDatePut, formattedDateRemoved, 0);
                finish();
            } else {
                Toast.makeText(this, "Error, diff time is not correct: " + Utils.getDateDiff(formattedDatePut, formattedDateRemoved, TimeUnit.MINUTES), Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
