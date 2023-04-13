package com.example.infocollection.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.util.SizeF;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

import com.example.infocollection.infomodel.BaseInfoModel;
import com.example.infocollection.ui.adapter.BaseAdapter;
import com.example.infocollection.ui.viewmodel.BaseViewModel;
import com.example.infocollection.utils.DecimalUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraFragment extends BaseFragment {
    @Override
    protected BaseViewModel setViewModel() {
        return ViewModelProviders.of(this).get(BaseViewModel.class);
    }

    @Override
    protected BaseAdapter setAdapter() {
        return new BaseAdapter(getContext());
    }

    @Override
    protected List<BaseInfoModel> getInfos() throws CameraAccessException, JSONException {
        Activity activity = getActivity();
        Context context = Objects.requireNonNull(activity).getApplicationContext();
        List<BaseInfoModel> infos = new ArrayList<>();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                if (manager == null) {
                    return infos;
                }
                String[] cameraIdinfos = manager.getCameraIdList();
                for (String cameraId : cameraIdinfos) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                    // 摄像头位置 后置 前置 外置
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    infos.add(new BaseInfoModel("摄像头", facing + " - " + getFacing(facing)));

                    // 分辨率
                    Rect activeArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    if (activeArraySize != null) {
                        int width = activeArraySize.right - activeArraySize.left;
                        int height = activeArraySize.bottom - activeArraySize.top;
                        double round = DecimalUtils.round(width * height / 1000000.0, 1);
                        infos.add(new BaseInfoModel("分辨率", round + " MP (" + width + "x" + height + ")"));
                    }

                    // 支持的光圈大小值
                    float[] lensInfoAvailableApertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                    if (lensInfoAvailableApertures != null && lensInfoAvailableApertures.length != 0) {
                        StringBuffer sb = new StringBuffer();
                        for (float f : lensInfoAvailableApertures) {
                            sb.append("f/").append(f).append(" ");
                        }
                        infos.add(new BaseInfoModel("光圈大小值", sb.toString().trim()));
                    }

                    // 支持的焦距列表
                    float[] lensInfoAvailableFocalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    if (lensInfoAvailableFocalLengths != null && lensInfoAvailableFocalLengths.length != 0) {
                        StringBuffer sb = new StringBuffer();
                        for (float f : lensInfoAvailableFocalLengths) {
                            sb.append(f).append(" mm").append(" ");
                        }
                        infos.add(new BaseInfoModel("焦距列表", sb.toString().trim()));
                    }

                    // 支持的自动对焦（AF）模式列表
                    int[] afAvailableModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                    if (afAvailableModes != null && afAvailableModes.length != 0) {
                        StringBuffer sb = new StringBuffer();
                        for (int i : afAvailableModes) {
                            sb.append(getAfAvailableModes(i)).append(",");
                        }
                        infos.add(new BaseInfoModel("支持的AF模式", sb.deleteCharAt(sb.length() - 1).toString()));
                    }

                    // 传感器尺寸
                    SizeF physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                    if (physicalSize != null) {
                        infos.add(new BaseInfoModel("传感器尺寸", physicalSize.getWidth() + "x" + physicalSize.getHeight()));
                    }

                    // 像素大小
                    if (physicalSize != null && activeArraySize != null) {
                        double value = physicalSize.getWidth() * physicalSize.getHeight();
                        double width = activeArraySize.right - activeArraySize.left;
                        double height = activeArraySize.bottom - activeArraySize.top;
                        double round = DecimalUtils.round(Math.sqrt((((value * 1000.0d) / width) * 1000.0d) / height), 2);
                        infos.add(new BaseInfoModel("像素大小", "~" + round + " µm"));
                    }

                    // 视角
                    float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    if (focalLengths != null && focalLengths.length > 0) {
                        float f = focalLengths[0];
                        SizeF sizeF = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                        if (sizeF != null) {
                            float width = sizeF.getWidth();
                            if (width > 0.0f) {
                                infos.add(new BaseInfoModel("视角", DecimalUtils.round(Math.toDegrees(Math.atan((width * 0.5d) / f)) * 2.0d, 1) + "°"));
                            }
                        }
                    }

                    // 支持的图像格式
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map != null) {
                        int[] ints = map.getOutputFormats();
                        StringBuffer sb = new StringBuffer();
                        for (int i : ints) {
                            sb.append(getFormat(i)).append(",");
                        }
                        infos.add(new BaseInfoModel("图像格式", sb.deleteCharAt(sb.length() - 1).toString()));
                    }

                    // 支持的感光度范围
                    Range<Integer> sensorInfoSensitivityRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                    if (sensorInfoSensitivityRange != null) {
                        infos.add(new BaseInfoModel("感光度范围", sensorInfoSensitivityRange.getLower() + "-" + sensorInfoSensitivityRange.getUpper()));
                    }

                    // 彩色滤光片在传感器上的布置
                    Integer sensorInfoColorFilterArrangement = characteristics.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT);
                    if (sensorInfoColorFilterArrangement != null) {
                        infos.add(new BaseInfoModel("彩色滤光片布置", getSensorInfoColorFilterArrangement(sensorInfoColorFilterArrangement)));
                    }

                    // 闪光灯
                    Boolean flashInfoAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    if (flashInfoAvailable != null) {
                        infos.add(new BaseInfoModel("闪光灯", flashInfoAvailable.toString()));
                    }

                    // ==================================================================================================

                    // 支持的像差校正模式列表
                    int[] aberrationModes = characteristics.get(CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES);
                    if (aberrationModes != null && aberrationModes.length != 0) {
                        JSONArray jsonArrayAberrationModes = new JSONArray();
                        for (int i : aberrationModes) {
                            jsonArrayAberrationModes.put(getAberrationModes(i));
                        }
                        infos.add(new BaseInfoModel("像差校正模式列表", jsonArrayAberrationModes.toString()));
                    }

                    // 支持的自动曝光防条纹模式列表
                    int[] antiBandingModes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
                    if (antiBandingModes != null && antiBandingModes.length != 0) {
                        JSONArray jsonArrayAntiBandingModes = new JSONArray();
                        for (int i : antiBandingModes) {
                            jsonArrayAntiBandingModes.put(getAntiBandingModes(i));
                        }
                        infos.add(new BaseInfoModel("自动曝光防条纹模式列表", jsonArrayAntiBandingModes.toString()));
                    }

                    // 支持的自动曝光模式列表
                    if (CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES != null) {
                        int[] aeAvailableModes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
                        if (aeAvailableModes != null && aeAvailableModes.length != 0) {
                            JSONArray jsonArrayAeAvailableModes = new JSONArray();
                            for (int i : aeAvailableModes) {
                                jsonArrayAeAvailableModes.put(getAeAvailableModes(i));
                            }
                            infos.add(new BaseInfoModel("自动曝光模式列表", jsonArrayAeAvailableModes.toString()));
                        }
                    }

                    // 最大和最小曝光补偿值
                    if (CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE != null) {
                        Range<Integer> compensationRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                        if (compensationRange != null) {
                            infos.add(new BaseInfoModel("最大和最小曝光补偿值", compensationRange.toString()));
                        }
                    }

                    // 可以更改曝光补偿的最小步长
                    if (CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP != null) {
                        Rational compensationStep = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
                        if (compensationStep != null) {
                            infos.add(new BaseInfoModel("可以更改曝光补偿的最小步长", compensationStep.doubleValue() + ""));
                        }
                    }

                    // 锁定自动曝光
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE != null) {
                        Boolean lockAvailable = characteristics.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE);
                        if (lockAvailable != null) {
                            infos.add(new BaseInfoModel("锁定自动曝光", lockAvailable + ""));
                        }
                    }

                    // 支持的色彩效果列表
                    if (CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS != null) {
                        int[] availableEffects = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
                        if (availableEffects != null && availableEffects.length != 0) {
                            JSONArray jsonArrayAvailableEffects = new JSONArray();
                            for (int i : availableEffects) {
                                jsonArrayAvailableEffects.put(getAvailableEffects(i));
                            }
                            infos.add(new BaseInfoModel("色彩效果列表", jsonArrayAvailableEffects.toString()));
                        }
                    }

                    // 支持的控制模式列表
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.CONTROL_AVAILABLE_MODES != null) {
                        int[] availableModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_MODES);
                        if (availableModes != null && availableModes.length != 0) {
                            JSONArray jsonArrayAvailableModes = new JSONArray();
                            for (int i : availableModes) {
                                jsonArrayAvailableModes.put(getAvailableModes(i));
                            }
                            infos.add(new BaseInfoModel("控制模式列表", jsonArrayAvailableModes.toString()));
                        }

                    }

                    // 支持的场景模式列表
                    if (CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES != null) {
                        int[] availableSceneModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
                        if (availableSceneModes != null && availableSceneModes.length != 0) {
                            JSONArray jsonArrayAvailableSceneModes = new JSONArray();
                            for (int i : availableSceneModes) {
                                jsonArrayAvailableSceneModes.put(getAvailableSceneModes(i));
                            }
                            infos.add(new BaseInfoModel("场景模式列表", jsonArrayAvailableSceneModes.toString()));
                        }
                    }

                    // 视频稳定模式列表
                    if (CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES != null) {
                        int[] videoStabilizationModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
                        if (videoStabilizationModes != null && videoStabilizationModes.length != 0) {
                            JSONArray jsonArrayVideoStabilizationModes = new JSONArray();
                            for (int i : videoStabilizationModes) {
                                jsonArrayVideoStabilizationModes.put(getVideoStabilizationModes(i));
                            }
                            infos.add(new BaseInfoModel("视频稳定模式列表", jsonArrayVideoStabilizationModes.toString()));
                        }
                    }

                    // 支持的自动白平衡模式列表
                    if (CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES != null) {
                        int[] awbAvailableModes = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
                        if (awbAvailableModes != null && awbAvailableModes.length != 0) {
                            JSONArray jsonArrayAwbAvailableModes = new JSONArray();
                            for (int i : awbAvailableModes) {
                                jsonArrayAwbAvailableModes.put(getAwbAvailableModes(i));
                            }
                            infos.add(new BaseInfoModel("自动白平衡模式列表", jsonArrayAwbAvailableModes.toString()));
                        }
                    }

                    // 是否支持自动白平衡
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE != null) {
                        Boolean awbLockAvailable = characteristics.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE);
                        if (awbLockAvailable != null) {
                            infos.add(new BaseInfoModel("支持自动白平衡", awbLockAvailable + ""));
                        }
                    }

                    // 自动曝光（AE）例程可以使用的最大测光区域数
                    if (CameraCharacteristics.CONTROL_MAX_REGIONS_AE != null) {
                        int maxRegionsAe = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
                        infos.add(new BaseInfoModel("AE例程可以使用的最大测光区域数", maxRegionsAe + ""));
                    }

                    // 自动对焦（AF）例程可以使用的最大测光区域数
                    if (CameraCharacteristics.CONTROL_MAX_REGIONS_AF != null) {
                        int maxRegionsAf = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
                        infos.add(new BaseInfoModel("AF例程可以使用的最大测光区域数", maxRegionsAf + ""));
                    }

                    // 自动白平衡（AWB）例程可以使用的最大测光区域数
                    if (CameraCharacteristics.CONTROL_MAX_REGIONS_AWB != null) {
                        int maxRegionsAwb = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB);
                        infos.add(new BaseInfoModel("AWB例程可以使用的最大测光区域数", maxRegionsAwb + ""));
                    }

                    // 相机设备支持的增强范围
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE != null) {
                        Range<Integer> rawSensitivityBoostRange = characteristics.get(CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE);
                        if (rawSensitivityBoostRange != null) {
                            infos.add(new BaseInfoModel("增强范围", rawSensitivityBoostRange.toString()));
                        }
                    }

                    // 支持的帧频范围列表
                    if (CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES != null) {
                        Range<Integer>[] fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                        if (fpsRanges != null && fpsRanges.length != 0) {
                            JSONArray jsonArrayFpsRanges = new JSONArray();
                            for (Range<Integer> i : fpsRanges) {
                                jsonArrayFpsRanges.put(i);
                            }
                            infos.add(new BaseInfoModel("帧频范围列表", jsonArrayFpsRanges.toString()));
                        }
                    }

                    // 本相机设备支持的失真校正模式列表
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && CameraCharacteristics.DISTORTION_CORRECTION_AVAILABLE_MODES != null) {
                        int[] correctionAvailableModes = characteristics.get(CameraCharacteristics.DISTORTION_CORRECTION_AVAILABLE_MODES);
                        if (correctionAvailableModes != null && correctionAvailableModes.length != 0) {
                            JSONArray jsonArrayCorrectionAvailableModes = new JSONArray();
                            for (int i : correctionAvailableModes) {
                                jsonArrayCorrectionAvailableModes.put(getCorrectionAvailableModes(i));
                            }
                            infos.add(new BaseInfoModel("失真校正模式列表", jsonArrayCorrectionAvailableModes.toString()));
                        }
                    }

                    // 支持的边缘增强模式列表
                    if (CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES != null) {
                        int[] availableEdgeModes = characteristics.get(CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES);
                        if (availableEdgeModes != null && availableEdgeModes.length != 0) {
                            JSONArray jsonArrayAvailableEdgeModes = new JSONArray();
                            for (int i : availableEdgeModes) {
                                jsonArrayAvailableEdgeModes.put(getAvailableEdgeModes(i));
                            }
                            infos.add(new BaseInfoModel("边缘增强模式列表", jsonArrayAvailableEdgeModes.toString()));
                        }
                    }

                    // 支持的热像素校正模式列表
                    if (CameraCharacteristics.HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES != null) {
                        int[] availableHotPixelModes = characteristics.get(CameraCharacteristics.HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES);
                        if (availableHotPixelModes != null && availableHotPixelModes.length != 0) {
                            JSONArray jsonArrayAvailableHotPixelModes = new JSONArray();
                            for (int i : availableHotPixelModes) {
                                jsonArrayAvailableHotPixelModes.put(getAvailableHotPixelModes(i));
                            }
                            infos.add(new BaseInfoModel("热像素校正模式列表", jsonArrayAvailableHotPixelModes.toString()));
                        }
                    }

                    // 摄像机设备制造商版本信息的简短字符串
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && CameraCharacteristics.INFO_VERSION != null) {
                        infos.add(new BaseInfoModel("制造商版本信息", characteristics.get(CameraCharacteristics.INFO_VERSION)));
                    }

                    // 此相机设备支持的JPEG缩略图尺寸列表
                    if (CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES != null) {
                        Size[] jpegAvailableThumbnailSizes = characteristics.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES);
                        JSONArray jsonArrayJpegAvailableThumbnailSizes = new JSONArray();
                        if (jpegAvailableThumbnailSizes != null && jpegAvailableThumbnailSizes.length != 0) {
                            for (Size s : jpegAvailableThumbnailSizes) {
                                jsonArrayJpegAvailableThumbnailSizes.put(s.toString());
                            }
                        }
                        infos.add(new BaseInfoModel("JPEG缩略图尺寸列表", jsonArrayJpegAvailableThumbnailSizes.toString()));
                    }

                    // 用于校正此相机设备的径向和切向镜头失真的校正系数
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && CameraCharacteristics.LENS_DISTORTION != null) {
                        float[] lensDistortion = characteristics.get(CameraCharacteristics.LENS_DISTORTION);
                        if (lensDistortion != null && lensDistortion.length != 0) {
                            infos.add(new BaseInfoModel("校正径向和切向失真的校正系数", new JSONArray(lensDistortion).toString()));
                        }
                    }

                    // 此相机设备支持的中性密度滤镜值列表
                    if (CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES != null) {
                        float[] lensInfoAvailableFilterDensities = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES);
                        if (lensInfoAvailableFilterDensities != null && lensInfoAvailableFilterDensities.length != 0) {
                            infos.add(new BaseInfoModel("中性密度滤镜值列表", new JSONArray(lensInfoAvailableFilterDensities).toString()));
                        }
                    }

                    // 本相机设备支持的光学防抖（OIS）模式列表
                    if (CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION != null) {
                        int[] availableOpticalStabilization = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
                        if (availableOpticalStabilization != null && availableOpticalStabilization.length != 0) {
                            JSONArray jsonArrayAvailableOpticalStabilization = new JSONArray();
                            for (int i : availableOpticalStabilization) {
                                jsonArrayAvailableOpticalStabilization.put(getAvailableOpticalStabilization(i));
                            }
                            infos.add(new BaseInfoModel("OIS模式列表", jsonArrayAvailableOpticalStabilization.toString()));
                        }
                    }

                    // 镜头焦距校准质量
                    if (CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION != null) {
                        Integer focusDistanceCalibration = characteristics.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
                        infos.add(new BaseInfoModel("焦距校准质量", getFocusDistanceCalibration(focusDistanceCalibration).toString()));
                    }

                    // 镜头的超焦距
                    if (CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE != null) {
                        float hyperFocalDistance = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);
                        infos.add(new BaseInfoModel("超焦距", hyperFocalDistance + ""));
                    }

                    // 距镜头最前面的最短距离，可使其聚焦
                    if (CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE != null) {
                        float minimumFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                        infos.add(new BaseInfoModel("最短聚焦距离", minimumFocusDistance + ""));
                    }

                    // 本相机设备固有校准的参数
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.LENS_INTRINSIC_CALIBRATION != null) {
                        float[] lensIntrinsicCalibration = characteristics.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION);
                        if (lensIntrinsicCalibration != null && lensIntrinsicCalibration.length != 0) {
                            infos.add(new BaseInfoModel("固有校准的参数", new JSONArray(lensIntrinsicCalibration).toString()));
                        }
                    }

                    // 镜头姿势
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && CameraCharacteristics.LENS_POSE_REFERENCE != null) {
                        Integer lensPoseReference = characteristics.get(CameraCharacteristics.LENS_POSE_REFERENCE);
                        infos.add(new BaseInfoModel("镜头姿势", getLensPoseReference(lensPoseReference)));
                    }

                    // 相机相对于传感器坐标系的方向
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.LENS_POSE_ROTATION != null) {
                        float[] lensPoseRotation = characteristics.get(CameraCharacteristics.LENS_POSE_ROTATION);
                        if (lensPoseRotation != null && lensPoseRotation.length != 0) {
                            infos.add(new BaseInfoModel("相对于传感器坐标系的方向", new JSONArray(lensPoseRotation).toString()));
                        }
                    }

                    // 相机光学中心的位置
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.LENS_POSE_TRANSLATION != null) {
                        float[] lensPoseTranslation = characteristics.get(CameraCharacteristics.LENS_POSE_TRANSLATION);
                        if (lensPoseTranslation != null && lensPoseTranslation.length != 0) {
                            infos.add(new BaseInfoModel("光学中心位置", new JSONArray(lensPoseTranslation).toString()));
                        }
                    }
                    // 帧时间戳同步
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && CameraCharacteristics.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE != null) {
                        Integer cameraSensorSyncType = characteristics.get(CameraCharacteristics.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE);
                        infos.add(new BaseInfoModel("帧时间戳同步类型", getCameraSensorSyncType(cameraSensorSyncType)));
                    }

                    // 本相机设备支持的降噪模式列表
                    if (CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES != null) {
                        int[] availableNoiseReductionModes = characteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);
                        if (availableNoiseReductionModes != null && availableNoiseReductionModes.length != 0) {
                            JSONArray jsonArrayAvailableNoiseReductionModes = new JSONArray();
                            for (int i : availableNoiseReductionModes) {
                                jsonArrayAvailableNoiseReductionModes.put(getAvailableNoiseReductionModes(i));
                            }
                            infos.add(new BaseInfoModel("降噪模式列表", jsonArrayAvailableNoiseReductionModes.toString()));
                        }
                    }

                    // 最大摄像机捕获流水线停顿
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.REPROCESS_MAX_CAPTURE_STALL != null) {
                        Integer maxCaptureStall = characteristics.get(CameraCharacteristics.REPROCESS_MAX_CAPTURE_STALL);
                        if (maxCaptureStall != null) {
                            infos.add(new BaseInfoModel("最大捕获流水线停顿", maxCaptureStall.toString()));
                        }
                    }

                    // 此相机设备宣传为完全支持的功能列表
                    if (CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES != null) {
                        int[] requestAvailableCapabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
                        if (requestAvailableCapabilities != null && requestAvailableCapabilities.length != 0) {
                            JSONArray jsonArrayRequestAvailableCapabilities = new JSONArray();
                            for (int i : requestAvailableCapabilities) {
                                jsonArrayRequestAvailableCapabilities.put(getRequestAvailableCapabilities(i));
                            }
                            infos.add(new BaseInfoModel("完全支持的功能列表", jsonArrayRequestAvailableCapabilities.toString()));
                        }
                    }

                    // 该相机设备支持的裁切类型
                    if (CameraCharacteristics.SCALER_CROPPING_TYPE != null) {
                        Integer scalerCroppingType = characteristics.get(CameraCharacteristics.SCALER_CROPPING_TYPE);
                        if (scalerCroppingType != null) {
                            infos.add(new BaseInfoModel("裁切类型", getScalerCroppingType(scalerCroppingType)));
                        }
                    }

                    //  此相机设备支持的传感器测试图案模式列表
                    if (CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES != null) {
                        int[] sensorAvailableTestPatternModes = characteristics.get(CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES);
                        if (sensorAvailableTestPatternModes != null && sensorAvailableTestPatternModes.length != 0) {
                            JSONArray jsonArraySensorAvailableTestPatternModes = new JSONArray();
                            for (int i : sensorAvailableTestPatternModes) {
                                jsonArraySensorAvailableTestPatternModes.put(getSensorAvailableTestPatternModes(i));
                            }
                            infos.add(new BaseInfoModel("传感器测试图案模式列表", jsonArraySensorAvailableTestPatternModes.toString()));
                        }
                    }

                    // 此相机设备支持的图像曝光时间范围
                    if (CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE != null) {
                        Range<Long> sensorInfoExposureTimeRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                        if (sensorInfoExposureTimeRange != null) {
                            infos.add(new BaseInfoModel("图像曝光时间范围", sensorInfoExposureTimeRange.toString()));
                        }
                    }

                    // 从本相机设备输出的RAW图像是否经过镜头阴影校正
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.SENSOR_INFO_LENS_SHADING_APPLIED != null) {
                        Boolean sensorInfoLensShadingApplied = characteristics.get(CameraCharacteristics.SENSOR_INFO_LENS_SHADING_APPLIED);
                        if (sensorInfoLensShadingApplied != null) {
                            infos.add(new BaseInfoModel("是否经过镜头阴影校正", sensorInfoLensShadingApplied.toString()));
                        }
                    }

                    // 本相机设备支持的最大可能帧时长
                    if (CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION != null) {
                        Long sensorInfoaxFrameDuration = characteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION);
                        if (sensorInfoaxFrameDuration != null) {
                            infos.add(new BaseInfoModel("最大可能帧时长", sensorInfoaxFrameDuration.toString()));
                        }
                    }

                    // 传感器输出的最大原始值
                    if (CameraCharacteristics.SENSOR_INFO_WHITE_LEVEL != null) {
                        Integer sensorInfoWhiteLevel = characteristics.get(CameraCharacteristics.SENSOR_INFO_WHITE_LEVEL);
                        if (sensorInfoWhiteLevel != null) {
                            infos.add(new BaseInfoModel("传感器输出最大原始值", sensorInfoWhiteLevel.toString()));
                        }
                    }

                    // 纯粹通过模拟增益实现的最大灵敏度
                    if (CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY != null) {
                        Integer sensorMaxAnalogSensitivity = characteristics.get(CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY);
                        if (sensorMaxAnalogSensitivity != null) {
                            infos.add(new BaseInfoModel("通过模拟增益实现的最大灵敏度", sensorMaxAnalogSensitivity.toString()));
                        }
                    }

                    // 用作场景光源的标准参考光源
                    if (CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT1 != null) {
                        Integer sensorReferenceIlluminant1 = characteristics.get(CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT1);
                        if (sensorReferenceIlluminant1 != null) {
                            infos.add(new BaseInfoModel("标准参考光源", getSensorReferenceIlluminant1(sensorReferenceIlluminant1)));
                        }
                    }

                    // 本相机设备支持的镜头阴影模式列表
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.SHADING_AVAILABLE_MODES != null) {
                        int[] shadingAvailableModes = characteristics.get(CameraCharacteristics.SHADING_AVAILABLE_MODES);
                        if (shadingAvailableModes != null && shadingAvailableModes.length != 0) {
                            JSONArray jsonArrayShadingAvailableModes = new JSONArray();
                            for (int i : shadingAvailableModes) {
                                jsonArrayShadingAvailableModes.put(getShadingAvailableModes(i));
                            }
                            infos.add(new BaseInfoModel("镜头阴影模式列表", jsonArrayShadingAvailableModes.toString()));
                        }
                    }

                    // 本相机设备支持的脸部识别模式列表
                    if (CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES != null) {
                        int[] availableFaceDetectModes = characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
                        if (availableFaceDetectModes != null && availableFaceDetectModes.length != 0) {
                            JSONArray jsonArrayAvailableFaceDetectModes = new JSONArray();
                            for (int i : availableFaceDetectModes) {
                                jsonArrayAvailableFaceDetectModes.put(getAvailableFaceDetectModes(i));
                            }
                            infos.add(new BaseInfoModel("脸部识别模式列表", jsonArrayAvailableFaceDetectModes.toString()));
                        }
                    }

                    // 本相机设备支持的镜头阴影贴图输出模式列表
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && CameraCharacteristics.STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES != null) {
                        int[] availableLensShadingMapModes = characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES);
                        if (availableLensShadingMapModes != null && availableLensShadingMapModes.length != 0) {
                            JSONArray jsonArrayAvailableLensShadingMapModes = new JSONArray();
                            for (int i : availableLensShadingMapModes) {
                                jsonArrayAvailableLensShadingMapModes.put(getAvailableLensShadingMapModes(i));
                            }
                            infos.add(new BaseInfoModel("镜头阴影贴图输出模式列表", jsonArrayAvailableLensShadingMapModes.toString()));
                        }
                    }

                    // 本相机设备支持的OIS数据输出模式列表
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && CameraCharacteristics.STATISTICS_INFO_AVAILABLE_OIS_DATA_MODES != null) {
                        int[] availableOisDataModes = characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_OIS_DATA_MODES);
                        if (availableOisDataModes != null && availableOisDataModes.length != 0) {
                            JSONArray jsonArrayAvailableOisDataModes = new JSONArray();
                            for (int i : availableOisDataModes) {
                                jsonArrayAvailableOisDataModes.put(getAvailableOisDataModes(i));
                            }
                            infos.add(new BaseInfoModel("OIS数据输出模式列表", jsonArrayAvailableOisDataModes.toString()));
                        }
                    }

                    // 同时可检测到的脸部的最大数量
                    if (CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT != null) {
                        Integer statisticsInfoMaxFaceCount = characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
                        if (statisticsInfoMaxFaceCount != null) {
                            infos.add(new BaseInfoModel("同时可检测到的脸部的最大数量", statisticsInfoMaxFaceCount.toString()));
                        }
                    }

                    // 本相机设备支持的色调映射模式列表
                    if (CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES != null) {
                        int[] availableToneMapModes = characteristics.get(CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES);
                        if (availableToneMapModes != null && availableToneMapModes.length != 0) {
                            JSONArray jsonArrayAvailableToneMapModes = new JSONArray();
                            for (int i : availableToneMapModes) {
                                jsonArrayAvailableToneMapModes.put(getAvailableToneMapModes(i));
                            }
                            infos.add(new BaseInfoModel("色调映射模式列表", jsonArrayAvailableToneMapModes.toString()));
                        }
                    }

                    // 色调图曲线中可用于的最大支持点数
                    if (CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS != null) {
                        Integer tonemapMaxCurvePoints = characteristics.get(CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS);
                        if (tonemapMaxCurvePoints != null) {
                            infos.add(new BaseInfoModel("色调图曲线最大支持点数", tonemapMaxCurvePoints.toString()));
                        }
                    }

                    // 下一个摄像头
                    infos.add(new BaseInfoModel("", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return infos;
    }

    private static String getAvailableToneMapModes(int availableToneMapModes) {
        switch (availableToneMapModes) {
            case CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE:
                return "CONTRAST_CURVE";
            case CaptureRequest.TONEMAP_MODE_FAST:
                return "FAST";
            case CaptureRequest.TONEMAP_MODE_GAMMA_VALUE:
                return "GAMMA_VALUE";
            case CaptureRequest.TONEMAP_MODE_HIGH_QUALITY:
                return "HIGH_QUALITY";
            case CaptureRequest.TONEMAP_MODE_PRESET_CURVE:
                return "PRESET_CURVE";
            default:
                return "Unknown" + "-" + availableToneMapModes;

        }
    }


    private static String getSyncMaxLatency(int syncMaxLatency) {
        switch (syncMaxLatency) {
            case CaptureRequest.SYNC_MAX_LATENCY_UNKNOWN:
                return "Unknown";
            case CaptureRequest.SYNC_MAX_LATENCY_PER_FRAME_CONTROL:
                return "PER_FRAME_CONTROL";
            default:
                return "Unknown" + "-" + syncMaxLatency;

        }
    }

    private static String getAvailableOisDataModes(int availableOisDataModes) {
        switch (availableOisDataModes) {
            case CaptureRequest.STATISTICS_OIS_DATA_MODE_ON:
                return "ON";
            case CaptureRequest.STATISTICS_OIS_DATA_MODE_OFF:
                return "OFF";
            default:
                return "Unknown" + "-" + availableOisDataModes;

        }
    }

    private static String getAvailableLensShadingMapModes(int availableLensShadingMapModes) {
        switch (availableLensShadingMapModes) {
            case CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON:
                return "ON";
            case CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_OFF:
                return "OFF";
            default:
                return "Unknown" + "-" + availableLensShadingMapModes;

        }
    }

    private static String getAvailableFaceDetectModes(int availableFaceDetectModes) {
        switch (availableFaceDetectModes) {
            case CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL:
                return "FULL";
            case CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE:
                return "SIMPLE";
            case CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF:
                return "OFF";
            default:
                return "Unknown" + "-" + availableFaceDetectModes;

        }
    }

    private static String getShadingAvailableModes(int shadingAvailableModes) {
        switch (shadingAvailableModes) {
            case CaptureRequest.SHADING_MODE_FAST:
                return "FAST";
            case CaptureRequest.SHADING_MODE_HIGH_QUALITY:
                return "HIGH_QUALITY";
            case CaptureRequest.SHADING_MODE_OFF:
                return "OFF";
            default:
                return "Unknown" + "-" + shadingAvailableModes;

        }
    }


    private static String getSensorReferenceIlluminant1(int sensorReferenceIlluminant1) {
        switch (sensorReferenceIlluminant1) {
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_CLOUDY_WEATHER:
                return "CLOUDY_WEATHER";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_COOL_WHITE_FLUORESCENT:
                return "COOL_WHITE_FLUORESCENT";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_D50:
                return "D50";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_D55:
                return "D55";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_D65:
                return "D65";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_D75:
                return "D75";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_DAY_WHITE_FLUORESCENT:
                return "DAY_WHITE_FLUORESCENT";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_DAYLIGHT:
                return "DAYLIGHT";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_DAYLIGHT_FLUORESCENT:
                return "DAYLIGHT_FLUORESCENT";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_FINE_WEATHER:
                return "FINE_WEATHER";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_FLASH:
                return "FLASH";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_FLUORESCENT:
                return "FLUORESCENT";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_ISO_STUDIO_TUNGSTEN:
                return "ISO_STUDIO_TUNGSTEN";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_SHADE:
                return "SHADE";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_A:
                return "STANDARD_A";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_B:
                return "STANDARD_B";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_C:
                return "STANDARD_C";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_TUNGSTEN:
                return "TUNGSTEN";
            case CaptureRequest.SENSOR_REFERENCE_ILLUMINANT1_WHITE_FLUORESCENT:
                return "WHITE_FLUORESCENT";

            default:
                return "Unknown" + "-" + sensorReferenceIlluminant1;

        }
    }

    private static String getSensorInfoTimestampSource(int sensorInfoTimestampSource) {
        switch (sensorInfoTimestampSource) {
            case CaptureRequest.SENSOR_INFO_TIMESTAMP_SOURCE_UNKNOWN:
                return "UNKNOWN";
            case CaptureRequest.SENSOR_INFO_TIMESTAMP_SOURCE_REALTIME:
                return "REALTIME";
            default:
                return "Unknown" + "-" + sensorInfoTimestampSource;

        }
    }

    private static String getSensorInfoColorFilterArrangement(int sensorInfoColorFilterArrangement) {
        switch (sensorInfoColorFilterArrangement) {
            case CaptureRequest.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR:
                return "BGGR";
            case CaptureRequest.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG:
                return "GBRG";
            case CaptureRequest.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG:
                return "GRBG";
            case CaptureRequest.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGB:
                return "RGB";
            case CaptureRequest.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB:
                return "RGGB";
            default:
                return "Unknown" + "-" + sensorInfoColorFilterArrangement;

        }
    }

    private static String getSensorAvailableTestPatternModes(int sensorAvailableTestPatternModes) {
        switch (sensorAvailableTestPatternModes) {
            case CaptureRequest.SENSOR_TEST_PATTERN_MODE_COLOR_BARS:
                return "COLOR_BARS";
            case CaptureRequest.SENSOR_TEST_PATTERN_MODE_COLOR_BARS_FADE_TO_GRAY:
                return "COLOR_BARS_FADE_TO_GRAY";
            case CaptureRequest.SENSOR_TEST_PATTERN_MODE_CUSTOM1:
                return "CUSTOM1";
            case CaptureRequest.SENSOR_TEST_PATTERN_MODE_OFF:
                return "OFF";
            case CaptureRequest.SENSOR_TEST_PATTERN_MODE_PN9:
                return "PN9";
            case CaptureRequest.SENSOR_TEST_PATTERN_MODE_SOLID_COLOR:
                return "SOLID_COLOR";
            default:
                return "Unknown" + "-" + sensorAvailableTestPatternModes;

        }
    }

    private static String getScalerCroppingType(int scalerCroppingType) {
        switch (scalerCroppingType) {
            case CaptureRequest.SCALER_CROPPING_TYPE_CENTER_ONLY:
                return "CENTER_ONLY";
            case CaptureRequest.SCALER_CROPPING_TYPE_FREEFORM:
                return "FREEFORM";
            default:
                return "Unknown" + "-" + scalerCroppingType;

        }
    }

    private static String getRequestAvailableCapabilities(int requestAvailableCapabilities) {
        switch (requestAvailableCapabilities) {
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE:
                return "BACKWARD_COMPATIBLE";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE:
                return "BURST_CAPTURE";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO:
                return "CONSTRAINED_HIGH_SPEED_VIDEO";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT:
                return "DEPTH_OUTPUT";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING:
                return "MANUAL_POST_PROCESSING";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA:
                return "LOGICAL_MULTI_CAMERA";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR:
                return "MANUAL_SENSOR";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME:
                return "MONOCHROME";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING:
                return "MOTION_TRACKING";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING:
                return "PRIVATE_REPROCESSING";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_RAW:
                return "RAW";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS:
                return "READ_SENSOR_SETTINGS";
            case CaptureRequest.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING:
                return "YUV_REPROCESSING";
            default:
                return "Unknown" + "-" + requestAvailableCapabilities;

        }
    }

    private static String getAvailableNoiseReductionModes(int availableNoiseReductionModes) {
        switch (availableNoiseReductionModes) {
            case CaptureRequest.NOISE_REDUCTION_MODE_FAST:
                return "FAST";
            case CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY:
                return "HIGH_QUALITY";
            case CaptureRequest.NOISE_REDUCTION_MODE_MINIMAL:
                return "MINIMAL";
            case CaptureRequest.NOISE_REDUCTION_MODE_OFF:
                return "OFF";
            case CaptureRequest.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG:
                return "ZERO_SHUTTER_LAG";
            default:
                return "Unknown" + "-" + availableNoiseReductionModes;

        }
    }

    private static String getCameraSensorSyncType(Integer cameraSensorSyncType) {
        if (cameraSensorSyncType == null) {
            return "Unknown";
        }
        switch (cameraSensorSyncType) {
            case CaptureRequest.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE_APPROXIMATE:
                return "APPROXIMATE";
            case CaptureRequest.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE_CALIBRATED:
                return "CALIBRATED";
            default:
                return "Unknown" + "-" + cameraSensorSyncType;

        }
    }

    private static String getLensPoseReference(Integer lensPoseReference) {
        if (lensPoseReference == null) {
            return "Unknown";
        }
        switch (lensPoseReference) {
            case CaptureRequest.LENS_POSE_REFERENCE_PRIMARY_CAMERA:
                return "PRIMARY_CAMERA";
            case CaptureRequest.LENS_POSE_REFERENCE_GYROSCOPE:
                return "GYROSCOPE";
            default:
                return "Unknown" + "-" + lensPoseReference;

        }
    }

    private static String getFocusDistanceCalibration(Integer focusDistanceCalibration) {
        if (focusDistanceCalibration == null) {
            return "Unknown";
        }
        switch (focusDistanceCalibration) {
            case CaptureRequest.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE:
                return "APPROXIMATE";
            case CaptureRequest.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED:
                return "CALIBRATED";
            case CaptureRequest.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED:
                return "UNCALIBRATED";
            default:
                return "Unknown" + "-" + focusDistanceCalibration;

        }
    }

    private static String getAvailableOpticalStabilization(int jsonArrayAvailableOpticalStabilization) {
        switch (jsonArrayAvailableOpticalStabilization) {
            case CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF:
                return "OFF";
            case CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON:
                return "ON";
            default:
                return "Unknown" + "-" + jsonArrayAvailableOpticalStabilization;

        }
    }


    private static String getAvailableHotPixelModes(int availableHotPixelModes) {
        switch (availableHotPixelModes) {
            case CaptureRequest.HOT_PIXEL_MODE_FAST:
                return "FAST";
            case CaptureRequest.HOT_PIXEL_MODE_HIGH_QUALITY:
                return "HIGH_QUALITY";
            case CaptureRequest.HOT_PIXEL_MODE_OFF:
                return "OFF";
            default:
                return "Unknown" + "-" + availableHotPixelModes;

        }
    }

    private static String getAvailableEdgeModes(int availableEdgeModes) {
        switch (availableEdgeModes) {
            case CaptureRequest.EDGE_MODE_FAST:
                return "FAST";
            case CaptureRequest.EDGE_MODE_HIGH_QUALITY:
                return "HIGH_QUALITY";
            case CaptureRequest.EDGE_MODE_OFF:
                return "OFF";
            case CaptureRequest.EDGE_MODE_ZERO_SHUTTER_LAG:
                return "ZERO_SHUTTER_LAG";
            default:
                return "Unknown" + "-" + availableEdgeModes;

        }
    }


    private static String getCorrectionAvailableModes(int correctionAvailableModes) {
        switch (correctionAvailableModes) {
            case CaptureRequest.DISTORTION_CORRECTION_MODE_FAST:
                return "FAST";
            case CaptureRequest.DISTORTION_CORRECTION_MODE_HIGH_QUALITY:
                return "HIGH_QUALITY";
            case CaptureRequest.DISTORTION_CORRECTION_MODE_OFF:
                return "OFF";
            default:
                return "Unknown" + "-" + correctionAvailableModes;

        }
    }

    private static String getAwbAvailableModes(int awbAvailableModes) {
        switch (awbAvailableModes) {
            case CaptureRequest.CONTROL_AWB_MODE_AUTO:
                return "AUTO";
            case CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT:
                return "CLOUDY_DAYLIGHT";
            case CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT:
                return "DAYLIGHT";
            case CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT:
                return "FLUORESCENT";
            case CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT:
                return "INCANDESCENT";
            case CaptureRequest.CONTROL_AWB_MODE_OFF:
                return "OFF";
            case CaptureRequest.CONTROL_AWB_MODE_SHADE:
                return "SHADE";
            case CaptureRequest.CONTROL_AWB_MODE_TWILIGHT:
                return "CONTROL_AWB_MODE_TWILIGHT";
            case CaptureRequest.CONTROL_AWB_MODE_WARM_FLUORESCENT:
                return "WARM_FLUORESCENT";
            default:
                return "Unknown" + "-" + awbAvailableModes;

        }
    }


    private static String getVideoStabilizationModes(int videoStabilizationModes) {
        switch (videoStabilizationModes) {
            case CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF:
                return "OFF";
            case CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON:
                return "ON";
            default:
                return "Unknown" + "-" + videoStabilizationModes;

        }
    }

    private static String getAvailableSceneModes(int availableSceneModes) {
        switch (availableSceneModes) {
            case CaptureRequest.CONTROL_SCENE_MODE_ACTION:
                return "ACTION";
            case CaptureRequest.CONTROL_SCENE_MODE_BARCODE:
                return "BARCODE";
            case CaptureRequest.CONTROL_SCENE_MODE_BEACH:
                return "BEACH";
            case CaptureRequest.CONTROL_SCENE_MODE_CANDLELIGHT:
                return "CANDLELIGHT";
            case CaptureRequest.CONTROL_SCENE_MODE_DISABLED:
                return "DISABLED";
            case CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY:
                return "FACE_PRIORITY";
            case CaptureRequest.CONTROL_SCENE_MODE_FIREWORKS:
                return "FIREWORKS";
            case CaptureRequest.CONTROL_SCENE_MODE_HDR:
                return "HDR";
            case CaptureRequest.CONTROL_SCENE_MODE_LANDSCAPE:
                return "LANDSCAPE";
            case CaptureRequest.CONTROL_SCENE_MODE_NIGHT:
                return "NIGHT";
            case CaptureRequest.CONTROL_SCENE_MODE_NIGHT_PORTRAIT:
                return "NIGHT_PORTRAIT";
            case CaptureRequest.CONTROL_SCENE_MODE_PARTY:
                return "PARTY";
            case CaptureRequest.CONTROL_SCENE_MODE_PORTRAIT:
                return "PORTRAIT";
            case CaptureRequest.CONTROL_SCENE_MODE_SNOW:
                return "SNOW";
            case CaptureRequest.CONTROL_SCENE_MODE_SPORTS:
                return "SPORTS";
            case CaptureRequest.CONTROL_SCENE_MODE_STEADYPHOTO:
                return "STEADYPHOTO";
            case CaptureRequest.CONTROL_SCENE_MODE_SUNSET:
                return "SUNSET";
            case CaptureRequest.CONTROL_SCENE_MODE_THEATRE:
                return "THEATRE";
            case CaptureRequest.CONTROL_SCENE_MODE_HIGH_SPEED_VIDEO:
                return "HIGH_SPEED_VIDEO";
            default:
                return "Unknown" + "-" + availableSceneModes;

        }
    }

    private static String getAvailableModes(int availableModes) {
        switch (availableModes) {
            case CaptureRequest.CONTROL_MODE_AUTO:
                return "AUTO";
            case CaptureRequest.CONTROL_MODE_OFF:
                return "OFF";
            case CaptureRequest.CONTROL_MODE_OFF_KEEP_STATE:
                return "OFF_KEEP_STATE";
            case CaptureRequest.CONTROL_MODE_USE_SCENE_MODE:
                return "MODE_USE_SCENE_MODE";
            default:
                return "Unknown" + "-" + availableModes;

        }
    }

    private static String getAvailableEffects(int availableEffects) {
        switch (availableEffects) {
            case CaptureRequest.CONTROL_EFFECT_MODE_OFF:
                return "OFF";
            case CaptureRequest.CONTROL_EFFECT_MODE_AQUA:
                return "AQUA";
            case CaptureRequest.CONTROL_EFFECT_MODE_BLACKBOARD:
                return "BLACKBOARD";
            case CaptureRequest.CONTROL_EFFECT_MODE_MONO:
                return "MONO";
            case CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE:
                return "NEGATIVE";
            case CaptureRequest.CONTROL_EFFECT_MODE_POSTERIZE:
                return "POSTERIZE";
            case CaptureRequest.CONTROL_EFFECT_MODE_SEPIA:
                return "SEPIA";
            case CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE:
                return "SOLARIZE";
            case CaptureRequest.CONTROL_EFFECT_MODE_WHITEBOARD:
                return "WHITEBOARD";
            default:
                return "Unknown" + "-" + availableEffects;

        }
    }

    private static String getAfAvailableModes(int afAvailableModes) {
        switch (afAvailableModes) {
            case CaptureRequest.CONTROL_AF_MODE_OFF:
                return "OFF";
            case CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE:
                return "CONTINUOUS_PICTURE";
            case CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO:
                return "CONTINUOUS_VIDEO";
            case CaptureRequest.CONTROL_AF_MODE_EDOF:
                return "EDOF";
            case CaptureRequest.CONTROL_AF_MODE_MACRO:
                return "MACRO";
            case CaptureRequest.CONTROL_AF_MODE_AUTO:
                return "AUTO";

            default:
                return "Unknown" + "-" + afAvailableModes;

        }
    }

    private static String getAeAvailableModes(int aeAvailableModes) {
        switch (aeAvailableModes) {
            case CaptureRequest.CONTROL_AE_MODE_OFF:
                return "OFF";
            case CaptureRequest.CONTROL_AE_MODE_ON:
                return "ON";
            case CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH:
                return "ON_ALWAYS_FLASH";
            case CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH:
                return "ON_AUTO_FLASH";
            case CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE:
                return "ON_AUTO_FLASH_REDEYE";
            case CaptureRequest.CONTROL_AE_MODE_ON_EXTERNAL_FLASH:
                return "ON_EXTERNAL_FLASH";

            default:
                return "Unknown" + "-" + aeAvailableModes;

        }
    }

    private static String getAntiBandingModes(int antiBandingModes) {
        switch (antiBandingModes) {
            case CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_50HZ:
                return "50HZ";
            case CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_60HZ:
                return "60HZ";
            case CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO:
                return "AUTO";
            case CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_OFF:
                return "OFF";
            default:
                return "Unknown" + "-" + antiBandingModes;

        }
    }

    private static String getAberrationModes(int aberrationModes) {
        switch (aberrationModes) {
            case CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_FAST:
                return "FAST";
            case CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY:
                return "HIGH_QUALITY";
            case CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF:
                return "OFF";
            default:
                return "Unknown" + "-" + aberrationModes;

        }
    }

    private static String getFacing(Integer facing) {
        if (facing == null) {
            return "Unknown";
        }
        switch (facing) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                return "FRONT";
            case CameraCharacteristics.LENS_FACING_BACK:
                return "BACK";
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                return "EXTERNAL";
            default:
                return "Unknown" + "-" + facing;
        }
    }

    private static String getLevel(Integer level) {
        if (level == null) {
            return "Unknown";
        }
        switch (level) {
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                return "LEGACY";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                return "LEVEL_3";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL:
                return "EXTERNAL";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                return "FULL";
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                return "LIMITED";
            default:
                return "Unknown" + "-" + level;
        }
    }

    private static String getFormat(int format) {
        switch (format) {
            case ImageFormat.DEPTH16:
                return "DEPTH16";
            case ImageFormat.DEPTH_POINT_CLOUD:
                return "DEPTH_POINT_CLOUD";
            case ImageFormat.FLEX_RGBA_8888:
                return "FLEX_RGBA_8888";
            case ImageFormat.FLEX_RGB_888:
                return "FLEX_RGB_888";
            case ImageFormat.JPEG:
                return "JPEG";
            case ImageFormat.NV16:
                return "NV16";
            case ImageFormat.NV21:
                return "NV21";
            case ImageFormat.PRIVATE:
                return "PRIVATE";
            case ImageFormat.RAW10:
                return "RAW10";
            case ImageFormat.RAW12:
                return "RAW12";
            case ImageFormat.RAW_PRIVATE:
                return "RAW_PRIVATE";
            case ImageFormat.RAW_SENSOR:
                return "RAW_SENSOR";
            case ImageFormat.RGB_565:
                return "RGB_565";
            case ImageFormat.YUV_420_888:
                return "YUV_420_888";
            case ImageFormat.YUV_422_888:
                return "YUV_422_888";
            case ImageFormat.YUV_444_888:
                return "YUV_444_888";
            case ImageFormat.YUY2:
                return "YUY2";
            case ImageFormat.YV12:
                return "YV12";
            default:
                return "Unknown" + "-" + format;
        }
    }
}
