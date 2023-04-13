package com.example.infocollection.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infocollection.R;
import com.example.infocollection.infomodel.SensorInfoModel;

import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {
    protected List<SensorInfoModel> data = null;
    protected Context context;

    public SensorAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(this.context).inflate(R.layout.item_sensor, parent, false);
        return new SensorViewHolder(root);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        SensorInfoModel info = data.get(position);
        holder.typeTv.setText(info.getType());
        holder.nameTv.setText(info.getName());
        holder.vendorTv.setText(info.getVendor());
        holder.versionTv.setText(Integer.toString(info.getVersion()));
        holder.resolutionTv.setText(Float.toString(info.getResolution()));
        holder.powerTv.setText(Float.toString(info.getPower()));
        holder.timeTv.setText(Long.toString(info.getTime()));

        StringBuilder valueString = new StringBuilder();
        valueString.append("");
        float[] values = info.getValues();
        for (int i = 0; i < values.length; i++) {
            valueString.append(String.format("value[%d]: %f\n", i, values[i]));
        }
        holder.valuesTv.setText(valueString);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<SensorInfoModel> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    static class SensorViewHolder extends RecyclerView.ViewHolder {
        TextView typeTv;
        TextView nameTv;
        TextView vendorTv;
        TextView versionTv;
        TextView resolutionTv;
        TextView powerTv;
        TextView timeTv;
        TextView valuesTv;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            this.typeTv = itemView.findViewById(R.id.tv_sensor_type);
            this.nameTv = itemView.findViewById(R.id.tv_sensor_name_value);
            this.vendorTv = itemView.findViewById(R.id.tv_sensor_vendor_value);
            this.versionTv = itemView.findViewById(R.id.tv_sensor_version_value);
            this.resolutionTv = itemView.findViewById(R.id.tv_sensor_resolution_value);
            this.powerTv = itemView.findViewById(R.id.tv_sensor_power_value);
            this.timeTv = itemView.findViewById(R.id.tv_sensor_time_value);
            this.valuesTv = itemView.findViewById(R.id.tv_sensor_value);
        }
    }
}
