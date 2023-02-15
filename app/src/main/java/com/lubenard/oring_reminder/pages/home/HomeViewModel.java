package com.lubenard.oring_reminder.pages.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<String> state = new MutableLiveData<>("Hello world");

    public LiveData<String> getState() {
        return state;
    }


}
