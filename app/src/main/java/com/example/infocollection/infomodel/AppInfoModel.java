package com.example.infocollection.infomodel;

import android.content.pm.PermissionInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;

// 描述应用程序的对象
public class AppInfoModel {
    private Drawable icon;
    private String name;
    private String pkgName;
    private String version;
    private int sdkVersion;

    private long firstInstallTime;

    private long lastUpdateTime;

    private String path;

    private Signature[] signatures;

    private PermissionInfo[] permissionInfos;

    public AppInfoModel(Drawable icon, String name, String pkgName, String version, int sdkVersion,
                        long firstInstallTime, long lastUpdateTime, String path, Signature[] signatures,
                        PermissionInfo[] permissionInfos) {
        this.icon = icon;
        this.name = name;
        this.pkgName = pkgName;
        this.version = version;
        this.sdkVersion = sdkVersion;
        this.firstInstallTime = firstInstallTime;
        this.lastUpdateTime = lastUpdateTime;
        this.path = path;
        this.signatures = signatures;
        this.permissionInfos = permissionInfos;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getVersion() {
        return version;
    }

    public int getSdkVersion() {
        return sdkVersion;
    }

    public long getFirstInstallTime() {
        return firstInstallTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public String getPath() {
        return path;
    }

    public Signature[] getSignatures() {
        return signatures;
    }

    public PermissionInfo[] getPermissionInfos() {
        return permissionInfos;
    }
}
