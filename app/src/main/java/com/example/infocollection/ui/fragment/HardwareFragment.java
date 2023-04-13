package com.example.infocollection.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;
import com.example.infocollection.utils.CommandUtils;
import com.example.infocollection.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class HardwareFragment extends BaseFragment{
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
        infos.add(new BaseInfoModel("主板名称", Build.BOARD, "The name of the underlying board, like \"goldfish\"."));
        infos.add(new BaseInfoModel("硬件名称", Build.HARDWARE, "The name of the hardware (from the kernel command line or /proc)."));
        infos.add(new BaseInfoModel("硬件制造商", Build.MANUFACTURER, "The manufacturer of the product/hardware."));
        infos.add(new BaseInfoModel("手机制造商", Build.PRODUCT, "The name of the overall product."));
        infos.add(new BaseInfoModel("屏幕分辨率", getScreenWidth(context)+"*"+getScreenHeight(context)));
        infos.add(new BaseInfoModel("屏幕分辨率适配", getDensityDpi(context) + " (" + getDensityId(context) + ")", "屏幕分辨率适配"));
        infos.add(new BaseInfoModel("屏幕刷新率", getRefreshRate(activity)+" Hz", "屏幕刷新率"));
        infos.add(new BaseInfoModel("设备参数", Build.DEVICE, "The name of the industrial design."));
        infos.add(new BaseInfoModel("基带版本", CommandUtils.getProperty("gsm.version.baseband"), "基带版本"));
        infos.add(new BaseInfoModel("无线电固件版本", Build.getRadioVersion(), "The radio firmware version is frequently not available when this class is initialized, leading to a blank or \"unknown\" value for this string."));
        return infos;
    }

    // 获取屏幕宽度
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    // 获取屏幕高度
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    // 获取Dpi
    public static int getDensityDpi(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.densityDpi;
    }

    // 获取像素密度等级
    public static String getDensityId(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.density;
        if (density < 1.0) {
            return "ldpi";
        } else if (density <= 1.0) {
            return "mdpi";
        } else if (density <= 1.5) {
            return "hdpi";
        } else if (density <= 2.0) {
            return "xhdpi";
        } else if (density <= 3.0) {
            return "xxhdpi";
        } else {
            return "xxxhdpi";
        }
    }

    public static int getRefreshRate(Activity activity) {
        return (int) activity.getWindowManager().getDefaultDisplay().getRefreshRate();
    }
}
