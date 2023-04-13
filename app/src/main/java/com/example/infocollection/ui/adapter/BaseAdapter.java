package com.example.infocollection.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.R;

import java.util.List;

public class BaseAdapter extends RecyclerView.Adapter<BaseAdapter.BaseViewHolder> {
    protected List<BaseInfoModel> data = null;
    protected Context context;

    public BaseAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(this.context).inflate(R.layout.item_base, parent, false);
        BaseViewHolder baseViewHolder = new BaseViewHolder(root);
        baseViewHolder.root.setOnClickListener(view -> {
            int position = baseViewHolder.getAdapterPosition();
            String desc = data.get(position).getBaseInfoDesc();
            Toast.makeText(root.getContext(), desc, Toast.LENGTH_LONG).show();
        });

        return baseViewHolder;
    }

    // 在这里绑定数据与视图
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        if (position % 2 == 0) {
            holder.root.setBackgroundColor(context.getResources().getColor(R.color.white));
        } else {
            holder.root.setBackgroundColor(context.getResources().getColor(R.color.item_bg_blue));
        }
        BaseInfoModel info = data.get(position);
        holder.infoKeyTv.setText(info.getBaseInfoKey());
        holder.infoValueTv.setText(info.getBaseInfoValue());
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    // 更新视图
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<BaseInfoModel> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder {
        View root;
        TextView infoKeyTv;
        TextView infoValueTv;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            this.infoKeyTv = itemView.findViewById(R.id.tv_info_key);
            this.infoValueTv = itemView.findViewById(R.id.tv_info_value);
        }
    }
}
