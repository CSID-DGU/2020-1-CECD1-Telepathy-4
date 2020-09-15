package com.example.betterfit.ui.contents;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ContentsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ContentsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is contents fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}