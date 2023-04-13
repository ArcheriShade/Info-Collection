package com.example.infocollection.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.infocollection.infomodel.ProcessInfoModel;

import java.util.List;

public class ProcessViewModel extends ViewModel {
    private final MutableLiveData<List<ProcessInfoModel>> mRecyclerView;

    public ProcessViewModel() {
        this.mRecyclerView = new MutableLiveData<>();
    }

    public LiveData<List<ProcessInfoModel>> getRecyclerView() {
        return this.mRecyclerView;
    }

    public void setValue(List<ProcessInfoModel> infoList) {
        this.mRecyclerView.setValue(infoList);
    }
}
