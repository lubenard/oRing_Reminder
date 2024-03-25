package com.lubenard.oring_reminder.pages.my_spermograms;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.custom_components.Spermograms;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;


public class MySpermogramsViewModel extends ViewModel {
    private static final String TAG = "MySpermogramsViewModel";
    private DbManager dbManager;

    public MutableLiveData<ArrayList<Spermograms>> spermoList = new MutableLiveData<>();

    public MySpermogramsViewModel() {
        dbManager = MainActivity.getDbManager();
    }

    void loadSpermoList() {
        ArrayList<Spermograms> dataModels = new ArrayList<>();
        LinkedHashMap<Integer, Spermograms> entrysDatas = dbManager.getAllSpermograms();
        for (LinkedHashMap.Entry<Integer, Spermograms> oneElemData : entrysDatas.entrySet())
            dataModels.add(oneElemData.getValue());
        spermoList.postValue(dataModels);
    }

    public void saveSpermoOnLocalStorage(Context context, Intent data) {
        String filename = new SimpleDateFormat("/dd-MM-yyyy_HH-mm-ss").format(new Date()) + ".pdf";
        Utils.Companion.writeFileOnInternalStorage(context, filename, data.getData());
        dbManager.importNewSpermo("file://" + context.getFilesDir().getAbsolutePath() + filename);
        Utils.Companion.generatePdfThumbnail(context, context.getFilesDir().getAbsolutePath() + filename);
    }
}