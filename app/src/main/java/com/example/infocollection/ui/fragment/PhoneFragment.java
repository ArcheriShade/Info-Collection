package com.example.infocollection.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.infomodel.SimInfoModel;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;
import com.example.infocollection.utils.CommandUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PhoneFragment extends BaseFragment {
    @Override
    protected BaseViewModel setViewModel() {
        return ViewModelProviders.of(this).get(BaseViewModel.class);
    }

    @Override
    protected BaseAdapter setAdapter() {
        return new BaseAdapter(getContext());
    }

    @Override
    protected List<BaseInfoModel> getInfos() {
        Activity activity = getActivity();
        Context context = Objects.requireNonNull(activity).getApplicationContext();
        List<BaseInfoModel> infos = new ArrayList<>();

        infos.add(new BaseInfoModel("AndroidId", getAndroidId(context)));
        infos.add(new BaseInfoModel("IMEI", getIMEI(context)));
        getDeviceInfo(context,infos);
        infos.add(new BaseInfoModel("ICCID", getIccId(context)));
        getSimInfo(context,infos);
        getOtherInfo(context,infos);

        return infos;
    }

    public static String getAndroidId(Context context) {
        try {
            return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static void getDeviceInfo(Context context, List<BaseInfoModel> list) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                list.add(new BaseInfoModel("IMEI2", tm.getImei(1)));
                // TODO 另外方式 CommandUtils.getProperty("persist.sys.meid")
                list.add(new BaseInfoModel("MEID", tm.getMeid()));
                list.add(new BaseInfoModel("MEID2", tm.getMeid(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            list.add(new BaseInfoModel("IMSI", tm.getSubscriberId()));
            list.add(new BaseInfoModel("SERIAL", getSerial()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSerial() {
        try {
            String serial = Build.SERIAL;
            if (TextUtils.isEmpty(serial)) {
                serial = CommandUtils.getProperty("no.such.thing");
            }
            if (TextUtils.isEmpty(serial)) {
                serial = CommandUtils.getProperty("ro.serialno");
            }
            if (TextUtils.isEmpty(serial)) {
                serial = CommandUtils.getProperty("ro.boot.serialno");
            }
            if (TextUtils.isEmpty(serial)) {
                serial = "Unknown";
            }
            return serial;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    @SuppressLint("MissingPermission")
    public static String getIccId(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSimSerialNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    @SuppressLint("MissingPermission")
    public static void getSimInfo(Context context, List<BaseInfoModel> list) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            list.add(new BaseInfoModel("SIM ISO", tm.getSimCountryIso()));
            list.add(new BaseInfoModel("SIM OP ID", tm.getSimOperator()));
            list.add(new BaseInfoModel("SIM OP NAME", tm.getSimOperatorName()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                list.add(new BaseInfoModel("SIM Id", tm.getSimCarrierId()+""));
                list.add(new BaseInfoModel("SIM IdName", tm.getSimCarrierIdName().toString()));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                list.add(new BaseInfoModel("SIM SpecificId", tm.getSimSpecificCarrierId() + ""));
                list.add(new BaseInfoModel("SIM SpecificIdName", tm.getSimSpecificCarrierIdName().toString()));
                list.add(new BaseInfoModel("SIM SpecificIdFromMM", tm.getCarrierIdFromSimMccMnc() + ""));
            }
            list.add(new BaseInfoModel("SIM STATE", tm.getSimState() + ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public static void getOtherInfo(Context context, List<BaseInfoModel> list) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                list.add(new BaseInfoModel("NET Specifier", tm.getNetworkSpecifier()));
            }
            list.add(new BaseInfoModel("NET ISO", tm.getNetworkCountryIso()));
            list.add(new BaseInfoModel("NET OP", tm.getNetworkOperator()));
            list.add(new BaseInfoModel("NET OP NAME", tm.getNetworkOperatorName()));
            list.add(new BaseInfoModel("NET TYPE", tm.getNetworkType() + ""));
            list.add(new BaseInfoModel("Device Soft Version", tm.getDeviceSoftwareVersion()));
            list.add(new BaseInfoModel("LINE NUMBER", tm.getLine1Number()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                list.add(new BaseInfoModel("MAN CODE", tm.getManufacturerCode()));
                list.add(new BaseInfoModel("Allocation Code", tm.getTypeAllocationCode()));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                list.add(new BaseInfoModel("MMS UA", tm.getMmsUserAgent()));
                list.add(new BaseInfoModel("MMS UA URL", tm.getMmsUAProfUrl()));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                list.add(new BaseInfoModel("NAI", tm.getNai()));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list.add(new BaseInfoModel("DATA NET TYPE", tm.getDataNetworkType() + ""));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                list.add(new BaseInfoModel("Phone Count", tm.getPhoneCount() + ""));
            }
            List<SimInfoModel> simInfoModels = querySimInfo(context);
            for (SimInfoModel ben : simInfoModels) {
                int simId = ben.getSimId();
                list.add(new BaseInfoModel("SIM " + simId + " ID", ben.getId() + ""));
                list.add(new BaseInfoModel("SIM " + simId + " ICCID", ben.getIccId()));
                list.add(new BaseInfoModel("SIM " + simId + " CarrierName", ben.getCarrierName()));
                list.add(new BaseInfoModel("SIM " + simId + " DisplayName", ben.getDisplayName()));
                list.add(new BaseInfoModel("SIM " + simId + " Number", ben.getNumber()));
                list.add(new BaseInfoModel("SIM " + simId + " MCC", ben.getMcc()));
                list.add(new BaseInfoModel("SIM " + simId + " MNC", ben.getMnc()));
            }
            getBuildInfo(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询数据库 SIM 信息 (同样需要 READ_PHONE_STATUS 权限)
     *
     * @param context
     * @return
     */
    private static List<SimInfoModel> querySimInfo(Context context) {
        List<SimInfoModel> list = new ArrayList<>();
        try {
            Uri uri = Uri.parse("content://telephony/siminfo"); //访问raw_contacts表
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(uri, new String[]{"_id", "icc_id", "sim_id", "display_name",
                    "carrier_name", "name_source", "color", "number", "display_number_format",
                    "data_roaming", "mcc", "mnc"}, "sim_id>=0", null, "sim_id");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id")));
                    @SuppressLint("Range") int simId = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sim_id")));
                    @SuppressLint("Range") String iccId = cursor.getString(cursor.getColumnIndex("icc_id"));
                    @SuppressLint("Range") String carrierName = cursor.getString(cursor.getColumnIndex("carrier_name"));
                    @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex("display_name"));
                    @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex("number"));
                    @SuppressLint("Range") String mcc = cursor.getString(cursor.getColumnIndex("mcc"));
                    @SuppressLint("Range") String mnc = cursor.getString(cursor.getColumnIndex("mnc"));
                    SimInfoModel info = new SimInfoModel(id, simId, iccId, carrierName, displayName, number, mcc, mnc);
                    list.add(info);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void getBuildInfo(List<BaseInfoModel> list) {
        String[] array = CommandUtils.exec("getprop");
        for (String line : array) {
            if (!TextUtils.isEmpty(line)
                    && (line.contains("imei")
                    || line.contains("iccid")
                    || line.contains("imsi")
                    || line.contains("meid")
                    || line.contains("serialno")
            )) {
                String[] split = line.split(":");
                if (split.length == 2) {
                    try {
                        if (!"[]".equals(split[1].trim())) {
                            list.add(new BaseInfoModel(split[0].trim(), split[1].trim()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }
}
