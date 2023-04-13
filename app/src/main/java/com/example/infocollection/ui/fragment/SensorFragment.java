package com.example.infocollection.ui.fragment;

import static android.content.Context.SENSOR_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import com.example.infocollection.infomodel.SensorInfoModel;
import com.example.infocollection.ui.adapter.SensorAdapter;
import com.example.infocollection.ui.viewmodel.SensorViewModel;
import com.example.infocollection.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;


public class SensorFragment extends Fragment {
    private Context context;
    private FragmentBaseBinding binding;
    private SensorViewModel sensorViewModel;
    private SensorAdapter adapter;
    private SensorManager sensorManager;
    private List<Sensor> sensors;
    List<SensorEventListener> sensorEventListeners;
    private Long time;
    private float[] values;
    private SwipeRefreshLayout swipeRefreshLayout; //下拉刷新布局
    private RecyclerView recyclerView; //可回收的线性布局
    protected static Handler mainHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getContext();
        sensorViewModel = ViewModelProviders.of(this).get(SensorViewModel.class);
        adapter = new SensorAdapter(context);

        // 设置传感器相关配置
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        setSensorEventListener();

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
        sensorViewModel.getRecyclerView().observe(getViewLifecycleOwner(), infos -> {
            swipeRefreshLayout.setRefreshing(false);
            adapter.updateData((sensorViewModel.getRecyclerView().getValue()));
        });

        return root;
    }

    // 设置传感器监听器
    private void setSensorEventListener() {
        sensorEventListeners = new ArrayList<>();
        for (int i = 0; i < sensors.size(); i++) {
            sensorEventListeners.add(new mSensorEventListener());
        }
        // 为每个传感器绑定监听器
        for (int i = 0; i < sensors.size(); i++) {
            sensorManager.registerListener(sensorEventListeners.get(i), sensors.get(i), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void refreshData() {
        ThreadPoolUtils.getInstance().execute(() -> {
            final List<SensorInfoModel> infos = getInfos();
            mainHandler.post(() -> sensorViewModel.setValue(infos));
        });
    }

    // 获取传感器信息
    private List<SensorInfoModel> getInfos() {
        List<SensorInfoModel> infos = new ArrayList<>();
        for (Sensor sensor : sensors) {
            String type = "Unknown";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
                type = sensor.getStringType().substring(15).replace("_", " ");
            }
            String name = sensor.getName();
            String vendor = sensor.getVendor();
            Integer version = sensor.getVersion();
            Float resolution = sensor.getResolution();
            Float power = sensor.getPower();

            infos.add(new SensorInfoModel(type, name, vendor, version, resolution, power, time, values));
        }

//        float[] values = new float[]{1.1f, 1.2f, 1.3f, 1.4f};
//        infos.add(new SensorInfoModel("加速度传感器", "Archeri", "Archeri", 1, 0.6f, 22.0f, 20160223L, values));

        return infos;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        // 传感器监听器解绑
        for (int i = 0; i < sensors.size(); i++) {
            sensorManager.unregisterListener(sensorEventListeners.get(i));
        }
        super.onDestroyView();
    }

    class mSensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            time = sensorEvent.timestamp;
            values = sensorEvent.values;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) { }
    }

}