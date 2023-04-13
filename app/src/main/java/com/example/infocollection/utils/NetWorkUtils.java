package com.example.infocollection.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.util.Pair;

import com.example.infocollection.infomodel.BaseInfoModel;

import java.lang.reflect.Method;
import java.util.List;

public class NetWorkUtils {

    // 判断是否有网络连接，并不代表可以数据访问
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    // 数据流量是否打开
    public static boolean isMobileEnabled(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 判断当前网络是否可以数据访问(测试不可靠)
    public static boolean isNetSystemUsable(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkCapabilities networkCapabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
                return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec("ping -c 3 www.baidu.com");
                return 0 == process.waitFor();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 判断是否是WiFi网络
    public static boolean isWifi(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }
        return false;
    }

    // 判断当前网络详细类型
    public static String getNetWorkType(Context context) {
        if (!isNetworkConnected(context)) {
            return "NONE";
        }
        if (isWifi(context)) {
            return "WIFI";
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            int networkType = telephonyManager.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return "GPRS";
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return "EDGE";
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return "CDMA";
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return "1xRTT";
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "IDEN";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return "UMTS";
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return "EVDO_0";
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return "EVDO_A";
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return "HSDPA";
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return "HSUPA";
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return "HSPA";
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return "EVDO_B";
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    return "EHRPD";
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "HSPAP";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "LTE";
                case TelephonyManager.NETWORK_TYPE_NR:
                    return "5G";
                default:
                    break;
            }
        }
        return "NONE";
    }

    // 网络信息
    public static void getNetWorkInfo(Context context, List<BaseInfoModel> list) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null) {
            list.add(new BaseInfoModel("NET类型名称", networkInfo.getTypeName()));
            String subName = networkInfo.getSubtypeName();
            if (!TextUtils.isEmpty(subName)) {
                list.add(new BaseInfoModel("NET子名称", subName));
            }
            list.add(new BaseInfoModel("NET名称", networkInfo.getExtraInfo()));
        }
    }

}
