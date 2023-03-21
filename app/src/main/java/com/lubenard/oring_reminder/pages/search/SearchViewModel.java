package com.lubenard.oring_reminder.pages.search;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchViewModel extends ViewModel {

    private DbManager dbManager;

    MutableLiveData<List<RingSession>> searchResults = new MutableLiveData<>();

    public SearchViewModel() {
        dbManager = MainActivity.getDbManager();
    }

    public void search(String date) {
        if (date != null)
            searchResults.setValue(dbManager.searchEntryInDb(date));
        else
            searchResults.setValue(Collections.emptyList());
    }
}
