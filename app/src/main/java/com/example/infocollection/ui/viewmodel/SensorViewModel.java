package com.example.infocollection.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.infocollection.infomodel.SensorInfoModel;

import java.util.List;

public class SensorViewModel extends ViewModel {
    private final MutableLiveData<List<SensorInfoModel>> mRecyclerView;

    public SensorViewModel() {
        this.mRecyclerView = new MutableLiveData<>();
    }

    public LiveData<List<SensorInfoModel>> getRecyclerView() {
        return this.mRecyclerView;
    }

    public void setValue(List<SensorInfoModel> infoList) {
        this.mRecyclerView.setValue(infoList);
    }
}
