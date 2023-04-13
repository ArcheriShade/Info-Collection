package com.example.infocollection.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.infocollection.R;
import com.example.infocollection.databinding.FragmentBaseBinding;
import com.example.infocollection.infomodel.ProcessInfoModel;
import com.example.infocollection.ui.adapter.ProcessAdapter;
import com.example.infocollection.ui.viewmodel.ProcessViewModel;
import com.example.infocollection.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

public class ProcessFragment extends Fragment {
    private Context context;
    private FragmentBaseBinding binding;
    private ProcessViewModel processViewModel;
    private ProcessAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout; //下拉刷新布局
    private RecyclerView recyclerView; //可回收的线性布局
    protected static Handler mainHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getContext();
        processViewModel = ViewModelProviders.of(this).get(ProcessViewModel.class);
        adapter = new ProcessAdapter(context);

        binding = FragmentBaseBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 设置下拉刷新样式
        swipeRefreshLayout = root.findViewById(R.id.srl_base);
        swipeRefreshLayout.setColorSchemeColors(R.color.theme_blue);
        // 监听下拉刷新事件：刷新获取信息
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(true);
            refreshData();
        });

        // 设置垂直线性布局
        recyclerView = root.findViewById(R.id.rv_base);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 设置数据变动监听
        processViewModel.getRecyclerView().observe(getViewLifecycleOwner(), infos -> {
            swipeRefreshLayout.setRefreshing(false);
            adapter.updateData((processViewModel.getRecyclerView().getValue()));
        });

        return root;
    }

    private void refreshData() {
        ThreadPoolUtils.getInstance().execute(() -> {
            final List<ProcessInfoModel> infos;
            infos = getInfos();
            mainHandler.post(() -> processViewModel.setValue(infos));
        });
    }

    private List<ProcessInfoModel> getInfos() {
        List<ProcessInfoModel> infos = new ArrayList<>();

//        // 获取所有进程的信息，需要第三方库
//        List<AndroidAppProcess> processInfos = ProcessManager.getRunningAppProcesses();

        // 或者使用UsageStatsManager

        PackageManager packageManager = context.getPackageManager();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 在安卓5.0之后，仅能获取当前APP的进程信息
        List<ActivityManager.RunningAppProcessInfo> processInfos =activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            try {
                // 进程名
                String processName = processInfo.processName;
                PackageInfo packageInfo = packageManager.getPackageInfo(processName, 0);
                // 进程图标
                Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
                // APP名
                String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                // 获取占用内存
                int[] pids = new int[]{processInfo.pid};
                Debug.MemoryInfo[] memoryInfos = activityManager.getProcessMemoryInfo(pids);
                long totalPrivateDirty = memoryInfos[0].getTotalPrivateDirty();

                infos.add(new ProcessInfoModel(icon, appName, totalPrivateDirty));
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return infos;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
