package com.example.infocollection.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PermissionInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infocollection.R;
import com.example.infocollection.infomodel.AppInfoModel;

import java.util.Date;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {
    protected List<AppInfoModel> data = null;
    protected Context context;

    public AppAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(this.context).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(root);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfoModel info = data.get(position);
        holder.iconImage.setImageDrawable(info.getIcon());
        holder.nameTv.setText(info.getName());
        holder.pkgNameTv.setText(info.getPkgName());
        holder.versionTv.setText(info.getVersion());
        holder.sdkVersionTv.setText(info.getSdkVersion()+"");
        holder.firstInstallTimeTv.setText(epochToDate(info.getFirstInstallTime()).toString());
        holder.lastUpdateTimeTv.setText(epochToDate(info.getLastUpdateTime()).toString());
        holder.pathTv.setText(info.getPath());
        holder.signatureTv.setText(info.getSignatures()[0].toString());

        StringBuilder permissionString = new StringBuilder();
        permissionString.append("");
        PermissionInfo[] permissionInfos = info.getPermissionInfos();
        if (permissionInfos != null) {
            for (int i = 0; i < permissionInfos.length; i++) {
                permissionString.append(String.format("permission[%d]: %s\n", i, permissionInfos[i].toString()));
            }
        }
        holder.permissionsTv.setText(permissionString);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<AppInfoModel> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public static Date epochToDate(long epochTime) {
        return new Date(epochTime * 1000);
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView nameTv;
        TextView pkgNameTv;
        TextView versionTv;
        TextView sdkVersionTv;
        TextView firstInstallTimeTv;
        TextView lastUpdateTimeTv;
        TextView pathTv;
        TextView signatureTv;
        TextView permissionsTv;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            this.iconImage = itemView.findViewById(R.id.image_app_icon);
            this.nameTv = itemView.findViewById(R.id.tv_app_name);
            this.pkgNameTv = itemView.findViewById(R.id.tv_app_pkgname_value);
            this.versionTv = itemView.findViewById(R.id.tv_app_version_value);
            this.sdkVersionTv = itemView.findViewById(R.id.tv_app_sdk_value);
            this.firstInstallTimeTv = itemView.findViewById(R.id.tv_app_install_time_value);
            this.lastUpdateTimeTv = itemView.findViewById(R.id.tv_app_update_time_value);
            this.pathTv = itemView.findViewById(R.id.tv_app_path_value);
            this.signatureTv = itemView.findViewById(R.id.tv_app_signature_value);
            this.permissionsTv = itemView.findViewById(R.id.tv_app_permission);
        }
    }
}
