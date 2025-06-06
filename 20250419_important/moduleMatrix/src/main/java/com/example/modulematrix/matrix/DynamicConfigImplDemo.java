package com.example.modulematrix.matrix;

import android.util.Log;

import com.tencent.matrix.util.MatrixLog;
import com.tencent.mrs.plugin.IDynamicConfig;

import java.util.concurrent.TimeUnit;

public class DynamicConfigImplDemo implements IDynamicConfig {
    private static final String TAG = "Matrix.DynamicConfigImplDemo";

    public DynamicConfigImplDemo() {

    }

    public boolean isFPSEnable() {
        return true;
    }

    public boolean isTraceEnable() {
        return true;
    }

    public boolean isSignalAnrTraceEnable() {
        return true;
    }

    public boolean isMatrixEnable() {
        return true;
    }

    @Override
    public String get(String key, String defStr) {
        //TODO here return default value which is inside sdk, you can change it as you wish. matrix-sdk-key in class MatrixEnum.

        // for Activity leak detect
        if ((ExptEnum.clicfg_matrix_resource_detect_interval_millis.name().equals(key) || ExptEnum.clicfg_matrix_resource_detect_interval_millis_bg.name().equals(key))) {
            Log.d("DynamicConfig", "Matrix.ActivityRefWatcher: clicfg_matrix_resource_detect_interval_millis 10s");
            return String.valueOf(TimeUnit.SECONDS.toMillis(5));
        }

        if (ExptEnum.clicfg_matrix_resource_max_detect_times.name().equals(key)) {
            Log.d("DynamicConfig", "Matrix.ActivityRefWatcher: clicfg_matrix_resource_max_detect_times 5");
            return String.valueOf(3);
        }

        return defStr;
    }


    @Override
    public int get(String key, int defInt) {
        //TODO here return default value which is inside sdk, you can change it as you wish. matrix-sdk-key in class MatrixEnum.

        if (MatrixEnum.clicfg_matrix_resource_max_detect_times.name().equals(key)) {
            MatrixLog.i(TAG, "key:" + key + ", before change:" + defInt + ", after change, value:" + 2);
            return 2;//new value
        }

        if (MatrixEnum.clicfg_matrix_trace_fps_report_threshold.name().equals(key)) {
            return 10000;
        }

        if (MatrixEnum.clicfg_matrix_trace_fps_time_slice.name().equals(key)) {
            return 12000;
        }

        return defInt;

    }

    @Override
    public long get(String key, long defLong) {
        //TODO here return default value which is inside sdk, you can change it as you wish. matrix-sdk-key in class MatrixEnum.
        if (MatrixEnum.clicfg_matrix_trace_fps_report_threshold.name().equals(key)) {
            return 10000L;
        }

        if (MatrixEnum.clicfg_matrix_resource_detect_interval_millis.name().equals(key)) {
            MatrixLog.i(TAG, key + ", before change:" + defLong + ", after change, value:" + 2000);
            return 2000;
        }

        return defLong;
    }


    @Override
    public boolean get(String key, boolean defBool) {
        //TODO here return default value which is inside sdk, you can change it as you wish. matrix-sdk-key in class MatrixEnum.

        return defBool;
    }

    @Override
    public float get(String key, float defFloat) {
        //TODO here return default value which is inside sdk, you can change it as you wish. matrix-sdk-key in class MatrixEnum.

        return defFloat;
    }

}
