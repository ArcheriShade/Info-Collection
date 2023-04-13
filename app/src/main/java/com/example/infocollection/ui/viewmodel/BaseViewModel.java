package com.example.infocollection.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.infocollection.infomodel.BaseInfoModel;

import java.util.List;

// 基本的信息对象模型
public class BaseViewModel extends ViewModel {
    private final MutableLiveData<List<BaseInfoModel>> mRecyclerView;

    public BaseViewModel() {
        this.mRecyclerView = new MutableLiveData<>();
    }

    public LiveData<List<BaseInfoModel>> getRecyclerView() {
        return this.mRecyclerView;
    }

    public void setValue(List<BaseInfoModel> infoList) {
        this.mRecyclerView.setValue(infoList);
    }
}
