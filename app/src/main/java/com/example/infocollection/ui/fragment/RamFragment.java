package com.example.infocollection.ui.fragment;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.format.Formatter;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RamFragment extends BaseFragment{
    @Override
    protected BaseViewModel setViewModel() {
        return ViewModelProviders.of(this).get(BaseViewModel.class);
    }

    @Override
    protected BaseAdapter setAdapter() {
        return new BaseAdapter(getContext());
    }

    @Override
    protected List<BaseInfoModel> getInfos() {
        Activity activity = getActivity();
        Context context = Objects.requireNonNull(activity).getApplicationContext();
        List<BaseInfoModel> infos = new ArrayList<>();

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalMem = memoryInfo.totalMem;
        long availMem = memoryInfo.availMem;
        long usedMem = totalMem - availMem;
        String totalMemStr = Formatter.formatFileSize(context, totalMem);
        String usedMemStr = Formatter.formatFileSize(context, usedMem);
        String availMemStr = Formatter.formatFileSize(context, availMem);

        infos.add(new BaseInfoModel("内存", "RAM"));
        infos.add(new BaseInfoModel("总大小", totalMemStr));
        infos.add(new BaseInfoModel("已用", usedMemStr));
        infos.add(new BaseInfoModel("可用", availMemStr));

        return infos;
    }
}
