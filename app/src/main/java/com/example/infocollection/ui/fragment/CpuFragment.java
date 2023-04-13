package com.example.infocollection.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProviders;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;
import com.example.infocollection.utils.CommandUtils;
import com.example.infocollection.utils.DecimalUtils;
import com.example.infocollection.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CpuFragment extends BaseFragment{
    @Override
    protected BaseViewModel setViewModel() {
        return ViewModelProviders.of(this).get(BaseViewModel.class);
    }

    @Override
    protected BaseAdapter setAdapter() {
        return new BaseAdapter(getContext());
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected List<BaseInfoModel> getInfos() {
        List<BaseInfoModel> infos = new ArrayList<>();
        infos.add(new BaseInfoModel("架构", CommandUtils.execute("uname -m")));
        setFrequency(infos);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (int i = 0; i < Build.SUPPORTED_ABIS.length; i++) {
                infos.add(new BaseInfoModel(String.format("Supported ABI[%d]", i), Build.SUPPORTED_ABIS[i], "CPU指令集"));
            }
        }
        // SoC系统级芯片信息
        infos.add(new BaseInfoModel("SoC", getSocInfo(), "系统级芯片信息"));
        return infos;
    }

    private static final FileFilter CPU_FILTER = pathname -> Pattern.matches("cpu[0-9]", pathname.getName());

    @SuppressLint("DefaultLocale")
    private static void setFrequency(List<BaseInfoModel> list) {
        try {
            int cores = Objects.requireNonNull(new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER)).length;
            list.add(new BaseInfoModel("核心数", cores + ""));
            if (cores > 0) {
                ArrayList<Integer> min = new ArrayList<>();
                ArrayList<Integer> max = new ArrayList<>();
                for (int i = 0; i < cores; i++) {
                    min.add(Integer.parseInt(FileUtils.readFile(String.format("/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_min_freq", i))));
                    max.add(Integer.parseInt(FileUtils.readFile(String.format("/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq", i))));
                }
                Collections.sort(min);
                Collections.sort(max);
                if (max.size() > 0) {
                    list.add(new BaseInfoModel("时钟速率", min.get(0) + " - " + max.get(max.size() - 1) + " MHz"));
                    Map<Integer, Integer> map = new HashMap<>();
                    for (int temp : max) {
                        Integer count = map.get(temp);
                        map.put(temp, (count == null) ? 1 : count + 1);
                    }
                    StringBuffer sb = new StringBuffer();
                    for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                        sb.append(entry.getValue())
                                .append(" x ")
                                .append(DecimalUtils.round(entry.getKey() / 1000.0 / 1000.0, 2))
                                .append(" GHz")
                                .append('\n');
                    }
                    list.add(new BaseInfoModel("簇", sb.deleteCharAt(sb.length() - 1).toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取SoC信号信息
    public static String getSocInfo() {
        String socStr = "";
        socStr = CommandUtils.execute("getprop ro.board.platform");
        if (TextUtils.isEmpty(socStr)) {
            socStr = CommandUtils.execute("getprop ro.hardware");
            if (TextUtils.isEmpty(socStr)) {
                socStr = CommandUtils.execute("getprop ro.boot.hardware");
            }
        }
        return socStr;
    }
}
