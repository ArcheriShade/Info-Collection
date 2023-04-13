package com.example.infocollection.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.infocollection.infomodel.AppInfoModel;

import java.util.List;

public class AppViewModel extends ViewModel {
    private final MutableLiveData<List<AppInfoModel>> mRecyclerView;

    public AppViewModel() {
        this.mRecyclerView = new MutableLiveData<>();
    }

    public LiveData<List<AppInfoModel>> getRecyclerView() {
        return this.mRecyclerView;
    }

    public void setValue(List<AppInfoModel> infoList) {
        this.mRecyclerView.setValue(infoList);
    }
}
