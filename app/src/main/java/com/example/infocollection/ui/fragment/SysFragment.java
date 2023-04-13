package com.example.infocollection.ui.fragment;

// 从系统中获取当前Build的信息(url:https://developer.android.google.cn/reference/android/os/Build?hl=en)
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.WebSettings;

import com.example.infocollection.R;
import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;
import com.example.infocollection.utils.CommandUtils;
import com.example.infocollection.utils.FileUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SysFragment extends BaseFragment {
    @Override
    protected BaseViewModel setViewModel() {
        return ViewModelProviders.of(this).get(BaseViewModel.class);
    }

    @Override
    protected BaseAdapter setAdapter() {
        return new BaseAdapter(getContext());
    }

    // 获取系统信息
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("DefaultLocale")
    @Override
    protected List<BaseInfoModel> getInfos() {
        Activity activity = getActivity();
        Context context = Objects.requireNonNull(activity).getApplicationContext();
        List<BaseInfoModel> infos = new ArrayList<>();
        infos.add(new BaseInfoModel("Base OS", Build.VERSION.BASE_OS, "The base OS build the product is based on."));
        infos.add(new BaseInfoModel("系统启动程序版本", Build.BOOTLOADER, "The system bootloader version number."));
        infos.add(new BaseInfoModel("系统定制商", Build.BRAND, "The consumer-visible brand with which the product/hardware will be associated, if any."));
        infos.add(new BaseInfoModel("当前开发代号", Build.VERSION.CODENAME, "The current development codename, or the string \"REL\" if this is a release build."));
        infos.add(new BaseInfoModel("唯一识别码", Build.FINGERPRINT, "A string that uniquely identifies this build."));
        infos.add(new BaseInfoModel("修订版本列表", Build.ID, "Either a changelist number, or a label like \"M4-rc20\"."));
        infos.add(new BaseInfoModel("源码控制版本", Build.VERSION.INCREMENTAL, "The internal value used by the underlying source control to represent this build."));
        infos.add(new BaseInfoModel("Model", Build.MODEL, "The end-user-visible name for the end product."));
        infos.add(new BaseInfoModel("用户可见版本字串", Build.VERSION.RELEASE, "The user-visible version string."));
        infos.add(new BaseInfoModel("版本或开发代号", Build.VERSION.RELEASE_OR_CODENAME, "The version string."));
        infos.add(new BaseInfoModel("SDK版本", Integer.toString(Build.VERSION.SDK_INT), "The SDK version of the software currently running on this hardware device."));
        infos.add(new BaseInfoModel("预览SDK版本", Integer.toString(Build.VERSION.PREVIEW_SDK_INT), "The developer preview revision of a prerelease SDK."));
        infos.add(new BaseInfoModel("安全补丁", Build.VERSION.SECURITY_PATCH, "The user-visible security patch level."));
        infos.add(new BaseInfoModel("Build ID", Build.DISPLAY, "A build ID string meant for displaying to the user."));
        infos.add(new BaseInfoModel("Build标签", Build.TAGS, "Comma-separated tags describing the build, like \"unsigned,debug\"."));
        infos.add(new BaseInfoModel("Build类型", Build.TYPE, "The type of build, like \"user\" or \"eng\"."));
        infos.add(new BaseInfoModel("Build生成时间", epochToDate(Build.TIME).toString(), "The time at which the build was produced, given in milliseconds since the UNIX epoch."));
        infos.add(new BaseInfoModel("JVM版本", System.getProperty("java.vm.version"), "Java VM版本"));
        infos.add(new BaseInfoModel("Host", Build.HOST, context.getString(R.string.info_unknown)));
        infos.add(new BaseInfoModel("User", Build.USER, context.getString(R.string.info_unknown)));
        infos.add(new BaseInfoModel("User Agent", getDefaultUserAgent(context)));
        infos.add(new BaseInfoModel("UUID", FileUtils.readFile("/proc/sys/kernel/random/uuid")));
        infos.add(new BaseInfoModel("系统语言", getCurrentLanguage(context), "系统语言"));
        infos.add(new BaseInfoModel("时区", getCurrentTimeZone(), "时区"));
        infos.add(new BaseInfoModel("调试模式开关", isOpenDebug(context)+"", "是否开启调试"));
        infos.add(new BaseInfoModel("调试状态", isDebugConnected()+"", "调试状态"));
        infos.add(new BaseInfoModel("USB调试状态", getUsbDebugStatus(), "USB调试状态"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            infos.add(new BaseInfoModel("虚拟位置状态", isAllowMockLocation(context)+"", "虚拟位置状态"));
        }
        try {
            infos.add(new BaseInfoModel("屏幕亮度", Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS) + ""));
            infos.add(new BaseInfoModel("自动屏幕亮度", (Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == 1) + ""));
            infos.add(new BaseInfoModel("自动屏幕旋转", (Settings.System.getInt(context.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION) == 1) + ""));
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        infos.add(new BaseInfoModel("SD卡状态", isSDCardMounted()+""));

        return infos;
    }

    public static Date epochToDate(long epochTime) {
        return new Date(epochTime * 1000);
    }

    public static String getCurrentLanguage(Context context) {
        return context.getResources().getConfiguration().locale.getDisplayLanguage();
    }

    public static String getCurrentTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getDisplayName(false, TimeZone.SHORT);
    }

    private static String getDefaultUserAgent(Context context) {
        String ua = null;
        try {
            ua = System.getProperty("http.agent");
            if (TextUtils.isEmpty(ua)) {
                Method localMethod = WebSettings.class.getDeclaredMethod("getDefaultUserAgent", new Class[]{Context.class});
                ua = (String) localMethod.invoke(WebSettings.class, new Object[]{context});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TextUtils.isEmpty(ua) ? "Unknown" : ua;
    }

    // 是否开启debug模式
    public static boolean isOpenDebug(Context context) {
        try {
            return (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 是否正在调试
    public static boolean isDebugConnected() {
        try {
            return android.os.Debug.isDebuggerConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 读取当前USB调试状态
    public static String getUsbDebugStatus() {
        return CommandUtils.execute("getprop init.svc.adbd");
    }

    // 判断是否打开了允许虚拟位置
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static boolean isAllowMockLocation(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                String providerStr = LocationManager.GPS_PROVIDER;
                LocationProvider provider = locationManager.getProvider(providerStr);
                if (provider != null) {
                    locationManager.addTestProvider(
                            provider.getName()
                            , provider.requiresNetwork()
                            , provider.requiresSatellite()
                            , provider.requiresCell()
                            , provider.hasMonetaryCost()
                            , provider.supportsAltitude()
                            , provider.supportsSpeed()
                            , provider.supportsBearing()
                            , provider.getPowerRequirement()
                            , provider.getAccuracy());
                } else {
                    locationManager.addTestProvider(
                            providerStr
                            , true, true, false, false, true, true, true
                            , ProviderProperties.POWER_USAGE_HIGH, ProviderProperties.ACCURACY_FINE);
                }
                locationManager.setTestProviderEnabled(providerStr, true);
                locationManager.setTestProviderStatus(providerStr, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
                // 模拟位置可用
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isSDCardMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
