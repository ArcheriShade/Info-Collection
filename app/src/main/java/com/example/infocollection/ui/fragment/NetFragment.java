package com.example.infocollection.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProviders;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;
import com.example.infocollection.utils.GatewayUtils;
import com.example.infocollection.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NetFragment extends BaseFragment{
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

        infos.add(new BaseInfoModel("Net可用状态", NetWorkUtils.isNetworkConnected(context) + ""));
        infos.add(new BaseInfoModel("Mobile可用状态", NetWorkUtils.isMobileEnabled(context) + ""));
        infos.add(new BaseInfoModel("WIFI可用状态", NetWorkUtils.isWifi(context) + ""));
        infos.add(new BaseInfoModel("NET类型", NetWorkUtils.getNetWorkType(context)));
        infos.add(new BaseInfoModel("网络系统状态", NetWorkUtils.isNetSystemUsable(context) + ""));
        NetWorkUtils.getNetWorkInfo(context, infos);

        Map<String, String> ips = GatewayUtils.getIp(context);
        if (ips.containsKey("en0")) {
            infos.add(new BaseInfoModel("IPv4", ips.get("en0")));
            infos.add(new BaseInfoModel("IPv6", GatewayUtils.getHostIpv6(ips.get("network_name"))));
        } else if (ips.containsKey("vpn")) {
            infos.add(new BaseInfoModel("IPv4", ips.get("vpn")));
        }
        infos.add(new BaseInfoModel("MAC地址", GatewayUtils.getMacAddress(context)));
        Pair<Integer, Integer> signal = GatewayUtils.getMobileSignal(context);
        infos.add(new BaseInfoModel("RSSI", signal.first + " dBm"));
        infos.add(new BaseInfoModel("网络级别", signal.second + ""));

        String proxyAddress = System.getProperty("http.proxyHost");
        String port = System.getProperty("http.proxyPort");
        int proxyPort = Integer.parseInt((port != null ? port : "-1"));
        boolean isProxy = (!TextUtils.isEmpty(proxyAddress) && (proxyPort != -1));
        if (isVpn(context)) {
            infos.add(new BaseInfoModel("VPN代理", "vpn"));
        } else if (isProxy) {
            infos.add(new BaseInfoModel("代理", "proxy"));
            infos.add(new BaseInfoModel("代理主机", proxyAddress));
            infos.add(new BaseInfoModel("代理端口", port));
        } else {
            infos.add(new BaseInfoModel("代理", "false"));
        }

        GatewayUtils.getProxyInfo(context, infos);

        boolean enable = false;
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            // 依赖 ACCESS_FINE_LOCATION、ACCESS_WIFI_STATE 权限，需要 WIFI 打开，TODO Android Q 之后 WiFi 扫描获取 BSSID 为随机生成
            List<ScanResult> scanResults = manager.getScanResults();
            enable = manager.isWifiEnabled();
            if (scanResults != null && !scanResults.isEmpty()) {
                for (ScanResult scanResult : scanResults) {
                    infos.add(new BaseInfoModel("WiFi SSID", scanResult.SSID));
                    infos.add(new BaseInfoModel("WiFi BSSID", scanResult.BSSID));
                    infos.add(new BaseInfoModel("WiFi Capabilities", scanResult.capabilities));
                    infos.add(new BaseInfoModel("WiFi Frequency", scanResult.frequency + ""));
                    infos.add(new BaseInfoModel("WiFi Level", scanResult.level + ""));
                }
            }
            try {
                // 依赖 CHANGE_WIFI_STATE 权限
                manager.startScan();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (infos.isEmpty()) {
            if (enable) {
                infos.add(new BaseInfoModel("WIFI Scan", "没有找都WiFi"));
            } else {
                infos.add(new BaseInfoModel("WIFI Scan", "Please turn on WiFi switch"));
            }
        }

        return infos;
    }

    private static boolean isVpn(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getNetworkInfo(17);
            return networkInfo.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
