package com.example.infocollection.ui.fragment;

import android.annotation.SuppressLint;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.R;
import com.example.infocollection.databinding.FragmentBaseBinding;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;
import com.example.infocollection.utils.ThreadPoolUtils;

import org.json.JSONException;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public abstract class BaseFragment extends Fragment {
    private FragmentBaseBinding binding;
    protected BaseViewModel baseViewModel;
    private BaseAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout; //下拉刷新布局
    private RecyclerView recyclerView; //可回收的线性布局
    protected static Handler mainHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseViewModel = setViewModel();
        adapter = setAdapter();

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
        baseViewModel.getRecyclerView().observe(getViewLifecycleOwner(), infos -> {
            swipeRefreshLayout.setRefreshing(false);
            adapter.updateData((baseViewModel.getRecyclerView().getValue()));
        });

        return root;
    }

    protected abstract BaseViewModel setViewModel();

    protected abstract BaseAdapter setAdapter();

    // 各种信息通过实现这个函数获取
    protected abstract List<BaseInfoModel> getInfos() throws CameraAccessException, JSONException;

    // 多线程刷新数据
    protected void refreshData() {
        ThreadPoolUtils.getInstance().execute(() -> {
            final List<BaseInfoModel> infos;
            try {
                infos = getInfos();
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            mainHandler.post(() -> baseViewModel.setValue(infos));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
