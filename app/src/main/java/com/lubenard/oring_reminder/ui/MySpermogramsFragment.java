package com.lubenard.oring_reminder.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.CustomListAdapter;
import com.lubenard.oring_reminder.custom_components.CustomSpermoListAdapter;
import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.custom_components.Spermograms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;


public class MySpermogramsFragment extends Fragment implements CustomSpermoListAdapter.onListItemClickListener{
    private static RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private static ArrayList<Spermograms> dataModels;
    private static DbManager dbManager;
    private static CustomSpermoListAdapter adapter;
    private static CustomSpermoListAdapter.onListItemClickListener onListItemClickListener;

    private final static String TAG = "MySpermogramsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.my_spermograms_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.my_spermo_title_fragment);

        FloatingActionButton fab = view.findViewById(R.id.fab);

        recyclerView = view.findViewById(R.id.spermo_list);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        onListItemClickListener = this;

        dataModels = new ArrayList<>();
        dbManager = MainActivity.getDbManager();

        fab.setOnClickListener(v -> selectSpermoFromFiles());
    }

    /**
     * onClickManager handling clicks on the spermogram List
     */
    @Override
    public void onListItemClickListener(int position) {
        Spermograms dataModel= dataModels.get(position);
        Log.d(TAG, "Element " + dataModel.getId());
        /*EntryDetailsFragment fragment = new EntryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("entryId", dataModel.getId());
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, null)
                .addToBackStack(null).commit();*/
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
        dataModels.clear();
        LinkedHashMap<Integer, Spermograms> entrysDatas = dbManager.getAllSpermograms();
        for (LinkedHashMap.Entry<Integer, Spermograms> oneElemData : entrysDatas.entrySet())
            dataModels.add(oneElemData.getValue());
        adapter = new CustomSpermoListAdapter(dataModels, onListItemClickListener);
        recyclerView.setAdapter(adapter);
    }

    private void selectSpermoFromFiles() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent dataToFileChooser = new Intent(Intent.ACTION_GET_CONTENT);
            dataToFileChooser.setType("application/pdf");
            try {
                startActivityForResult(dataToFileChooser, 1);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Failed to open a Intent to import Spermogram.");
                Toast.makeText(getContext(), R.string.toast_error_custom_path_backup_restore_fail, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Android version too old to select file from folder");
            Toast.makeText(getContext(), R.string.toast_error_custom_path_backup_restore_android_too_old, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getDataString() != null) {
            String filename = new SimpleDateFormat("/dd-MM-yyyy_HH-mm-ss").format(new Date()) + ".pdf";
            writeFileOnInternalStorage(getContext(), filename, data.getData());
            dbManager.importNewSpermo("file://" + getContext().getFilesDir().getAbsolutePath() + filename);
        }
    }

    /**
     * Copty a file into internal storage
     * @param mcoContext
     * @param sFileName name to new file
     * @param datasUri Uri of file to copy
     */
    // Very useful https://mkyong.com/java/how-to-write-to-file-in-java-fileoutputstream-example/
    public static void writeFileOnInternalStorage(Context mcoContext, String sFileName, Uri datasUri) {
        try {
            File file = new File(mcoContext.getFilesDir(), sFileName);
            FileOutputStream fop = new FileOutputStream(file);
            InputStream inputStream = mcoContext.getContentResolver().openInputStream(datasUri);
            while (inputStream.available() > 0)
                fop.write(inputStream.read());
            fop.close();
            Log.d(TAG, "Wrote file to " + file.getAbsolutePath());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
