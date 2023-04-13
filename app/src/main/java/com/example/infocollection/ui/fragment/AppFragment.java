package com.example.infocollection.ui.fragment;

import static android.content.pm.PackageManager.GET_PERMISSIONS;
import static android.content.pm.PackageManager.GET_SIGNATURES;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.infocollection.R;
import com.example.infocollection.databinding.FragmentBaseBinding;
import com.example.infocollection.infomodel.AppInfoModel;
import com.example.infocollection.ui.adapter.AppAdapter;
import com.example.infocollection.ui.viewmodel.AppViewModel;
import com.example.infocollection.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

public class AppFragment extends Fragment {
    private Context context;
    private FragmentBaseBinding binding;
    private AppViewModel appViewModel;
    private AppAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout; //下拉刷新布局
    private RecyclerView recyclerView; //可回收的线性布局
    protected static Handler mainHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getContext();
        appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        adapter = new AppAdapter(context);

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
        appViewModel.getRecyclerView().observe(getViewLifecycleOwner(), infos -> {
            swipeRefreshLayout.setRefreshing(false);
            adapter.updateData((appViewModel.getRecyclerView().getValue()));
        });

        return root;
    }

    private void refreshData() {
        ThreadPoolUtils.getInstance().execute(() -> {
            final List<AppInfoModel> infos;
            try {
                infos = getInfos();
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            mainHandler.post(() -> appViewModel.setValue(infos));
        });
    }

    private List<AppInfoModel> getInfos() throws PackageManager.NameNotFoundException {
        List<AppInfoModel> infos = new ArrayList<>();

        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
        for (PackageInfo pkgInfo : installedPackages) {
            String packageName = pkgInfo.packageName;
            @SuppressLint("PackageManagerGetSignatures") Signature[] signatures = packageManager.getPackageInfo(packageName, GET_SIGNATURES).signatures;
            PermissionInfo[] permissionInfos = packageManager.getPackageInfo(packageName, GET_PERMISSIONS).permissions;
            AppInfoModel appInfo = new AppInfoModel(
                    pkgInfo.applicationInfo.loadIcon(packageManager),
                    pkgInfo.applicationInfo.loadLabel(packageManager).toString(),
                    pkgInfo.packageName,
                    pkgInfo.versionName,
                    pkgInfo.applicationInfo.targetSdkVersion,
                    pkgInfo.firstInstallTime,
                    pkgInfo.lastUpdateTime,
                    pkgInfo.applicationInfo.dataDir,
                    signatures,
                    permissionInfos
            );
            infos.add(appInfo);
        }

        return infos;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
