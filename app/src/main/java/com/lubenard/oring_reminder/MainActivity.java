package com.lubenard.oring_reminder;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity {

    private ArrayList<RingModel> dataModels;
    private DbManager dbManager;
    private CustomListAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEntry();
            }
        });

        listView = findViewById(R.id.main_list);

        dataModels = new ArrayList<>();

        dbManager = new DbManager(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RingModel dataModel= dataModels.get(i);
                Log.d("ONCLICK", "Element " + dataModel.getId());
                Intent intent = new Intent(getApplicationContext(), EntryDetails.class);
                intent.putExtra("entryId", dataModel.getId());
                startActivity(intent);
            }
        });
    }

    private void updateElementList() {
        LinkedHashMap<Integer, RingModel> contactsdatas = dbManager.getAllDatasForMainList();
        for (LinkedHashMap.Entry<Integer, RingModel> oneElemDatas : contactsdatas.entrySet()) {
            dataModels.add(oneElemDatas.getValue());
        }
        adapter = new CustomListAdapter(dataModels, getApplicationContext());
        listView.setAdapter(adapter);
    }

    private void createNewEntry() {
        Intent intent = new Intent(this, EditEntry.class);
        intent.putExtra("entryId", -1);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Launch settings page
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment(), null)
                    .addToBackStack(null).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dataModels.clear();
        updateElementList();
    }
}