package com.example.infocollection.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infocollection.R;
import com.example.infocollection.infomodel.ProcessInfoModel;

import java.util.List;

public class ProcessAdapter extends RecyclerView.Adapter<ProcessAdapter.ProcessViewHolder> {
    protected List<ProcessInfoModel> data = null;
    protected Context context;

    public ProcessAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ProcessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(this.context).inflate(R.layout.item_process, parent, false);
        return new ProcessViewHolder(root);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ProcessViewHolder holder, int position) {
        if (position % 2 == 0) {
            holder.root.setBackgroundColor(context.getResources().getColor(R.color.white));
        } else {
            holder.root.setBackgroundColor(context.getResources().getColor(R.color.item_bg_blue));
        }
        ProcessInfoModel info = data.get(position);
        holder.iconImage.setImageDrawable(info.getIcon());
        holder.nameTv.setText(info.getName());
        holder.usedMemTv.setText("占用内存: " + Formatter.formatFileSize(context, info.getUsedMem()));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<ProcessInfoModel> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    static class ProcessViewHolder extends RecyclerView.ViewHolder {
        View root;
        ImageView iconImage;
        TextView nameTv;
        TextView usedMemTv;

        public ProcessViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            this.iconImage = itemView.findViewById(R.id.image_process_icon);
            this.nameTv = itemView.findViewById(R.id.tv_process_name);
            this.usedMemTv = itemView.findViewById(R.id.tv_used_mem);
        }
    }
}
