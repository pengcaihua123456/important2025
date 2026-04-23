package com.evenbus.myapplication.leak;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;

import com.kwai.koom.base.CommonConfig;
import com.kwai.koom.base.MonitorLog;
import com.kwai.koom.base.MonitorManager;
import com.kwai.koom.javaoom.monitor.OOMHprofUploader;
import com.kwai.koom.javaoom.monitor.OOMMonitor;
import com.kwai.koom.javaoom.monitor.OOMMonitorConfig;
import com.kwai.koom.javaoom.monitor.OOMReportUploader;
import com.kwai.koom.nativeoom.leakmonitor.LeakMonitor;
import com.kwai.koom.nativeoom.leakmonitor.LeakMonitorConfig;
import com.kwai.koom.nativeoom.leakmonitor.LeakRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class KOOMConfig {

    private static final String TAG = "KOOMConfig";

    // ========== Java 监控配置 ==========
    private static final int THREAD_THRESHOLD = 50;
    private static final int FD_THRESHOLD = 300;
    private static final float HEAP_THRESHOLD = 0.9f;
    private static final int VSS_SIZE_THRESHOLD = 1_000_000;
    private static final int MAX_OVER_THRESHOLD_COUNT = 1;
    private static final int ANALYSIS_MAX_TIMES_PER_VERSION = 3;
    private static final long ANALYSIS_PERIOD_PER_VERSION = 15L * 24L * 60L * 60L * 1000L;
    private static final long LOOP_INTERVAL = 5_000L;

    // ========== Native 监控配置 ==========
    private static final long NATIVE_LOOP_INTERVAL = 5000L;
    private static final int NATIVE_MONITOR_THRESHOLD = 16;
    private static final long NATIVE_HEAP_THRESHOLD = 10 * 1024 * 1024;

    private Application application;

    public void init(Application application) {
        this.application = application;

        // 1. 初始化 Java 堆监控
        initJavaMonitor(application);

        // 2. 初始化 Native 堆监控
        initNativeMonitor(application);

        MonitorLog.i(TAG, "KOOM 全部监控模块初始化完成");
    }

    /**
     * Java 堆监控初始化
     */
    private void initJavaMonitor(Application application) {
        // 1. 构建 CommonConfig
        CommonConfig commonConfig = buildCommonConfig(application);

        // ⚠️ 关键：必须先设置 CommonConfig 到 MonitorManager
        MonitorManager.INSTANCE.initCommonConfig(commonConfig);

        // 2. 构建 OOMMonitorConfig
        OOMMonitorConfig monitorConfig = buildMonitorConfig();

        // 3. 初始化 OOMMonitor
        OOMMonitor.INSTANCE.init(commonConfig, monitorConfig);

        // 4. 启动监控循环
        OOMMonitor.INSTANCE.startLoop(true, false, 5000L);

        MonitorLog.i(TAG, "Java 堆监控已启动");
    }

    /**
     * Native 堆监控初始化
     */
    private void initNativeMonitor(Application application) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            MonitorLog.w(TAG, "Native 监控需要 Android 7.0+，当前版本：" + Build.VERSION.SDK_INT);
            return;
        }

        try {
            LeakMonitorConfig config = new LeakMonitorConfig.Builder()
                    .setLoopInterval(NATIVE_LOOP_INTERVAL)
                    .setMonitorThreshold(NATIVE_MONITOR_THRESHOLD)
                    .setNativeHeapAllocatedThreshold((int) NATIVE_HEAP_THRESHOLD)
                    .setSelectedSoList(new String[0])
                    .setIgnoredSoList(new String[]{"libc", "libc++"})
                    .setEnableLocalSymbolic(false)
                    .setLeakListener(leaks -> {
                        if (leaks == null || leaks.isEmpty()) {
                            return;
                        }
                        MonitorLog.w(TAG, "========== 检测到 Native 内存泄漏 ==========");
                        for (LeakRecord leak : leaks) {
                            MonitorLog.w(TAG, "泄漏信息: " + leak.toString());
                        }
//                        handleNativeLeak(leaks);
                    })
                    .build();

            MonitorManager.INSTANCE.addMonitorConfig(config);
            LeakMonitor.INSTANCE.start();

            MonitorLog.i(TAG, "Native 堆监控已启动 | 间隔=" + NATIVE_LOOP_INTERVAL + "ms");

        } catch (Exception e) {
            MonitorLog.e(TAG, "Native 监控初始化失败: " + e.getMessage());
        }
    }

    private void handleNativeLeak(Set<LeakRecord> leaks) {
        for (LeakRecord leak : leaks) {
            MonitorLog.i(TAG, "NativeLeak: " + leak.toString());
        }
    }

    private CommonConfig buildCommonConfig(Application application) {
        return new CommonConfig.Builder()
                .setApplication(application)
                .setVersionNameInvoker(new Function0<String>() {
                    @Override
                    public String invoke() {
                        try {
                            return application.getPackageManager()
                                    .getPackageInfo(application.getPackageName(), 0)
                                    .versionName;
                        } catch (Exception e) {
                            return "1.0.0";
                        }
                    }
                })
                .setDebugMode(true)
                .setSdkVersionMatch(Build.VERSION.SDK_INT >= 21)
                .setRootFileInvoker(new Function1<String, File>() {
                    @Override
                    public File invoke(String path) {
                        File dir = new File(application.getCacheDir(), "koom/" + path);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        return dir;
                    }
                })
                .setSharedPreferencesInvoker(new Function1<String, SharedPreferences>() {
                    @Override
                    public SharedPreferences invoke(String name) {
                        return application.getSharedPreferences("koom_prefs", android.content.Context.MODE_PRIVATE);
                    }
                })
                .setSharedPreferencesKeysInvoker(new Function1<SharedPreferences, Set<String>>() {
                    @Override
                    public Set<String> invoke(SharedPreferences sp) {
                        return sp.getAll().keySet();
                    }
                })
                .build();
    }

    private OOMMonitorConfig buildMonitorConfig() {
        return new OOMMonitorConfig.Builder()
                .setThreadThreshold(THREAD_THRESHOLD)
                .setFdThreshold(FD_THRESHOLD)
                .setHeapThreshold(HEAP_THRESHOLD)
                .setVssSizeThreshold(VSS_SIZE_THRESHOLD)
                .setMaxOverThresholdCount(MAX_OVER_THRESHOLD_COUNT)
                .setAnalysisMaxTimesPerVersion(ANALYSIS_MAX_TIMES_PER_VERSION)
                .setAnalysisPeriodPerVersion((int) ANALYSIS_PERIOD_PER_VERSION)
                .setLoopInterval(LOOP_INTERVAL)
                .setEnableHprofDumpAnalysis(true)
                .setHprofUploader(createHprofUploader())
                .setReportUploader(createReportUploader())
                .build();
    }

    private OOMHprofUploader createHprofUploader() {
        return new OOMHprofUploader() {
            @Override
            public void upload(File file, OOMHprofUploader.HprofType type) {
                MonitorLog.i(TAG, "=== Hprof 文件生成 ===");
                MonitorLog.i(TAG, "文件路径: " + file.getAbsolutePath());
                MonitorLog.i(TAG, "文件大小: " + file.length() / 1024 + " KB");
                saveHprofInfo(file, type);
            }

            private void saveHprofInfo(File file, OOMHprofUploader.HprofType type) {
                try {
                    File infoFile = new File(application.getCacheDir(), "koom/hprof_info.txt");
                    FileWriter writer = new FileWriter(infoFile, true);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    writer.write(sdf.format(new Date()) + " | ");
                    writer.write("路径: " + file.getAbsolutePath() + " | ");
                    writer.write("大小: " + file.length() / 1024 + "KB\n");
                    writer.close();
                } catch (IOException e) {
                    MonitorLog.e(TAG, "保存hprof信息失败: " + e.getMessage());
                }
            }
        };
    }

    private OOMReportUploader createReportUploader() {
        return new OOMReportUploader() {
            @Override
            public void upload(File file, String content) {
                MonitorLog.i(TAG, "=== 内存泄漏报告 ===");
                MonitorLog.i(TAG, "报告内容: " + content);
                saveReportToFile(file, content);
            }

            private void saveReportToFile(File reportFile, String content) {
                try {
                    File logDir = new File(application.getCacheDir(), "koom/reports");
                    if (!logDir.exists()) {
                        logDir.mkdirs();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    File backupFile = new File(logDir, "leak_report_" + sdf.format(new Date()) + ".json");
                    FileWriter writer = new FileWriter(backupFile);
                    writer.write(content);
                    writer.close();
                    MonitorLog.i(TAG, "报告已保存: " + backupFile.getAbsolutePath());
                } catch (IOException e) {
                    MonitorLog.e(TAG, "保存报告失败: " + e.getMessage());
                }
            }
        };
    }
}