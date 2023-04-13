package com.example.infocollection.infomodel;

import java.util.List;

// 描述传感器信息的对象
public class SensorInfoModel {
    private final String type;
    private final String name;
    private final String vendor;
    private final Integer version;
    private final Float resolution;
    private final Float power;
    private final Long time;
    private final float[] values;

    public SensorInfoModel(String type, String name, String vendor, Integer version, Float resolution, Float power, Long time, float[] values) {
        this.type = type;
        this.name = name;
        this.vendor = vendor;
        this.version = version;
        this.resolution = resolution;
        this.power = power;
        this.time = time;
        this.values = values;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getVendor() {
        return vendor;
    }

    public Integer getVersion() {
        return version;
    }

    public Float getResolution() {
        return resolution;
    }

    public Float getPower() {
        return power;
    }

    public Long getTime() {
        return time;
    }

    public float[] getValues() {
        return values;
    }
}
