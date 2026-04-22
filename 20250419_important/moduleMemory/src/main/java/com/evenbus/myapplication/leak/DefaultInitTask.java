package com.evenbus.myapplication.leak;

import android.app.Application;
import android.os.Build;

import com.kwai.koom.base.CommonConfig;
import com.kwai.koom.base.MonitorManager;

import kotlin.jvm.functions.Function0;

public class DefaultInitTask {

    public static Application mApplication;

    public static void init(Application application) {
        mApplication= application;
        // 构建 CommonConfig
        CommonConfig config = new CommonConfig.Builder()
                .setApplication(application)
                .setDebugMode(true)
                // 设置版本名
                .setVersionNameInvoker(new Function0<String>() {
                    @Override
                    public String invoke() {
                        return "1.0.0";
                    }
                })
                // 设置 SDK 版本是否支持
                .setSdkVersionMatch(
                        Build.VERSION.SDK_INT <= 34 &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                )
                .build();

        // 初始化 MonitorManager 并调用 onApplicationCreate
        MonitorManager.initCommonConfig(config);
        MonitorManager.onApplicationCreate();
    }
}