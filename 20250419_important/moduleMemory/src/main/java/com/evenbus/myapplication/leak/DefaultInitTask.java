package com.evenbus.myapplication.leak;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.kwai.koom.base.CommonConfig;
import com.kwai.koom.base.MonitorManager;

import kotlin.jvm.functions.Function0;

public class DefaultInitTask {

    private static final String TAG = "DefaultInitTask";

    public static Application mApplication;
    public static CommonConfig sCommonConfig;

    public static void init(Application application) {
        Log.i(TAG, "========== DefaultInitTask 初始化开始 ==========");
        Log.i(TAG, "进程名: " + getProcessName(application));

        mApplication = application;

        CommonConfig config = new CommonConfig.Builder()
                .setApplication(application)
                .setDebugMode(true)
                .setVersionNameInvoker(new Function0<String>() {
                    @Override
                    public String invoke() {
                        try {
                            String version = application.getPackageManager()
                                    .getPackageInfo(application.getPackageName(), 0)
                                    .versionName;
                            Log.i(TAG, "获取版本名: " + version);
                            return version;
                        } catch (Exception e) {
                            return "1.0.0";
                        }
                    }
                })
                .setSdkVersionMatch(
                        Build.VERSION.SDK_INT <= 34 &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                )
                .build();

        sCommonConfig = config;
        Log.i(TAG, "CommonConfig 构建完成: " + config);

        MonitorManager.initCommonConfig(config);
        Log.i(TAG, "MonitorManager.initCommonConfig 完成");

        MonitorManager.onApplicationCreate();
        Log.i(TAG, "MonitorManager.onApplicationCreate 完成");
        Log.i(TAG, "========== DefaultInitTask 初始化完成 ==========");
    }

    private static String getProcessName(Application application) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            return Application.getProcessName();
        }
        return "unknown";
    }
}