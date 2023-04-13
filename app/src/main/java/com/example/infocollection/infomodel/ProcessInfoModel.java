package com.example.infocollection.infomodel;

import android.graphics.drawable.Drawable;

public class ProcessInfoModel {
    private Drawable icon;
    private String name;
    private long usedMem;

    public ProcessInfoModel(Drawable icon, String name, long usedMem) {
        this.icon = icon;
        this.name = name;
        this.usedMem = usedMem;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public long getUsedMem() {
        return usedMem;
    }
}
