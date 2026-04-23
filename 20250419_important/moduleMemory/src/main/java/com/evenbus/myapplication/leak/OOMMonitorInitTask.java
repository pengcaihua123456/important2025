package com.evenbus.myapplication.leak;


import android.app.Application;

import com.kwai.koom.base.InitTask;
import com.kwai.koom.base.MonitorLog;
import com.kwai.koom.base.MonitorManager;
import com.kwai.koom.javaoom.monitor.OOMHprofUploader;
import com.kwai.koom.javaoom.monitor.OOMMonitorConfig;
import com.kwai.koom.javaoom.monitor.OOMReportUploader;

import java.io.File;

public class OOMMonitorInitTask implements InitTask {


    public void start(){
        OOMMonitorInitTask.getInstance().init(DefaultInitTask.mApplication);
    }

    private static volatile OOMMonitorInitTask instance;

    private OOMMonitorInitTask() {
        // 私有构造函数，防止外部实例化
    }

    public static OOMMonitorInitTask getInstance() {
        if (instance == null) {
            synchronized (OOMMonitorInitTask.class) {
                if (instance == null) {
                    instance = new OOMMonitorInitTask();
                }
            }
        }
        return instance;
    }

    @Override
    public void init(Application application) {
        OOMMonitorConfig config = new OOMMonitorConfig.Builder()
                .setThreadThreshold(50) // 50 only for test! Please use default value!
                .setFdThreshold(300) // 300 only for test! Please use default value!
                .setHeapThreshold(0.9f) // 0.9f for test! Please use default value!
                .setVssSizeThreshold(1_000_000) // 1_000_000 for test! Please use default value!
                .setMaxOverThresholdCount(1) // 1 for test! Please use default value!
                .setAnalysisMaxTimesPerVersion(3) // Consider use default value！
                .setAnalysisPeriodPerVersion((int)(15 * 24 * 60 * 60 * 1000L)) // Consider use default value！
                .setLoopInterval(5_000) // 5_000 for test! Please use default value!
                .setEnableHprofDumpAnalysis(true)
                .setHprofUploader(new OOMHprofUploader() {
                    @Override
                    public void upload(File file, OOMHprofUploader.HprofType type) {
                        MonitorLog.e("OOMMonitor", "todo, upload hprof " + file.getName() + " if necessary");
                    }
                })
                .setReportUploader(new OOMReportUploader() {
                    @Override
                    public void upload(File file, String content) {
                        MonitorLog.i("OOMMonitor", content);
                        MonitorLog.e("OOMMonitor", "todo, upload report " + file.getName() + " if necessary");
                    }
                })
                .build();

        MonitorManager.addMonitorConfig(config);
    }
}
