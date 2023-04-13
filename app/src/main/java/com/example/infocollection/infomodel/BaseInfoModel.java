package com.example.infocollection.infomodel;

// 描述基本信息的抽象String三元组
public class BaseInfoModel {
    private final String baseInfoKey;
    private final String baseInfoValue;
    private final String baseInfoDesc;

    public BaseInfoModel(String baseInfoKey, String baseInfoValue, String baseInfoDesc) {
        this.baseInfoKey = baseInfoKey;
        this.baseInfoValue = baseInfoValue;
        this.baseInfoDesc = baseInfoDesc;
    }

    public BaseInfoModel(String baseInfoKey, String baseInfoValue) {
        this.baseInfoKey = baseInfoKey;
        this.baseInfoValue = baseInfoValue;
        this.baseInfoDesc = baseInfoKey;
    }

    public String getBaseInfoKey() {
        return baseInfoKey;
    }

    public String getBaseInfoValue() {
        return baseInfoValue;
    }

    public String getBaseInfoDesc() {
        return baseInfoDesc;
    }
}
