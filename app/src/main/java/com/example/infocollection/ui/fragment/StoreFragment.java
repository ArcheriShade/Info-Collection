package com.example.infocollection.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.format.Formatter;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class StoreFragment extends BaseFragment {

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

        File card = Environment.getExternalStorageDirectory();
        long totalSpace = card.getTotalSpace();
        long freeSpace = card.getFreeSpace();
        long usedSpace = totalSpace - freeSpace;
        String path = card.getAbsolutePath();
        String totalSpaceStr = Formatter.formatFileSize(context, totalSpace);
        String usedSpaceStr = Formatter.formatFileSize(context, usedSpace);
        String freeSpaceStr = Formatter.formatFileSize(context, freeSpace);

        infos.add(new BaseInfoModel("存储", "Store"));
        infos.add(new BaseInfoModel("总大小", totalSpaceStr));
        infos.add(new BaseInfoModel("已用", usedSpaceStr));
        infos.add(new BaseInfoModel("可用", freeSpaceStr));
        infos.add(new BaseInfoModel("绝对路径", path));

        return infos;
    }
}
