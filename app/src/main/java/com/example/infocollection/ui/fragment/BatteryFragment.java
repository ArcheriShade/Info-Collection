package com.example.infocollection.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;
import com.example.infocollection.utils.DecimalUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BatteryFragment extends BaseFragment {
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

        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            double batteryLevel = -1;
            if (level != -1 && scale != -1) {
                batteryLevel = DecimalUtils.divide((double) level, (double) scale);
            }
            // unknown=1, charging=2, discharging=3, not charging=4, full=5
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            // ac=1, usb=2, wireless=4
            int plugState = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            // unknown=1, good=2, overheat=3, dead=4, over voltage=5, unspecified failure=6, cold=7
            int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            boolean present = batteryStatus.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
            String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

            infos.add(new BaseInfoModel("电池存在情况", present + ""));
            infos.add(new BaseInfoModel("电量", DecimalUtils.mul(batteryLevel, 100d) + " %"));
            infos.add(new BaseInfoModel("健康状况", batteryHealth(health)));
            infos.add(new BaseInfoModel("状态", batteryStatus(status)));
            infos.add(new BaseInfoModel("电源", batteryPlugged(plugState)));
            infos.add(new BaseInfoModel("技术", technology));
            infos.add(new BaseInfoModel("温度", temperature / 10 + " ℃"));
            if (voltage > 1000) {
                infos.add(new BaseInfoModel("电压", voltage / 1000f + "V"));
            } else {
                infos.add(new BaseInfoModel("电压", voltage + "V"));
            }
            infos.add(new BaseInfoModel("平均电流", getAverageCurrent(context) + ""));
            infos.add(new BaseInfoModel("电池容量", getBatteryCapacity(context)));
        }
        return infos;
    }

    private static int getAverageCurrent(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 获取电池容量
    @SuppressLint("PrivateApi")
    public static String getBatteryCapacity(Context context) {
        double batteryCapacity = 0;
        try {
            Object mPowerProfile;
            final String powerProfileClass = "com.android.internal.os.PowerProfile";
            mPowerProfile = Class.forName(powerProfileClass)
                    .getConstructor(Context.class)
                    .newInstance(context);
            batteryCapacity = (double) Class.forName(powerProfileClass)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return batteryCapacity + " mAh";
    }

    // 健康情况
    private static String batteryHealth(int status) {
        String health = "Unknown";
        switch (status) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                health = "Cold";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                health = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                health = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                health = "OverVoltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                health = "Overheat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                health = "Unknown";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                health = "Unspecified";
                break;
            default:
                break;
        }
        return health;
    }

    // 充电状态
    private static String batteryStatus(int status) {
        String batteryStatus = "Unknown";
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                batteryStatus = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                batteryStatus = "DisCharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                batteryStatus = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                batteryStatus = "NotCharging";
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                batteryStatus = "Unknown";
                break;
            default:
                break;
        }
        return batteryStatus;
    }

    // 电源
    private static String batteryPlugged(int status) {
        String plugged = "Unknown";
        switch (status) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                plugged = "AC";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                plugged = "USB";
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                plugged = "Wireless";
                break;
            default:
                break;
        }
        return plugged;
    }
}
