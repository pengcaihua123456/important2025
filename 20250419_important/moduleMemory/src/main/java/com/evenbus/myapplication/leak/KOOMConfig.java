package com.evenbus.myapplication.leak;

import android.app.Application;
import android.content.SharedPreferences;

import com.kwai.koom.base.CommonConfig;
import com.kwai.koom.base.MonitorLog;
import com.kwai.koom.javaoom.monitor.OOMHprofUploader;
import com.kwai.koom.javaoom.monitor.OOMMonitor;
import com.kwai.koom.javaoom.monitor.OOMMonitorConfig;
import com.kwai.koom.javaoom.monitor.OOMReportUploader;

import java.io.File;
import java.util.Set;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class KOOMConfig {

    private static final String TAG = "KOOMConfig";

    // 阈值配置常量（仅测试用，生产环境请使用默认值）
    private static final int THREAD_THRESHOLD = 50;
    private static final int FD_THRESHOLD = 300;
    private static final float HEAP_THRESHOLD = 0.9f;
    private static final int VSS_SIZE_THRESHOLD = 1_000_000;
    private static final int MAX_OVER_THRESHOLD_COUNT = 1;

    // 分析配置常量
    private static final int ANALYSIS_MAX_TIMES_PER_VERSION = 3;
    private static final long ANALYSIS_PERIOD_PER_VERSION = 15L * 24L * 60L * 60L * 1000L;
    private static final long LOOP_INTERVAL = 5_000L;

    public void init(Application application) {
        // 1. 构建 CommonConfig
        CommonConfig commonConfig = buildCommonConfig(application);

        // 2. 构建 OOMMonitorConfig
        OOMMonitorConfig monitorConfig = buildMonitorConfig();

        // 3. 初始化 OOMMonitor
        OOMMonitor.INSTANCE.init(commonConfig, monitorConfig);

        // 4. 启动循环监控
        OOMMonitor.INSTANCE.startLoop(true, false, 5000L);

        MonitorLog.i(TAG, "KOOM 初始化完成并启动监控");
    }

    /**
     * 构建 CommonConfig
     */
    private CommonConfig buildCommonConfig(Application application) {
        return new CommonConfig.Builder()
                // 必需：设置 Application
                .setApplication(application)

                // 必需：设置版本名获取器
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

                // 可选：设置调试模式（默认 true）
                .setDebugMode(true)

                // 可选：设置 SDK 版本匹配（默认 false）
                .setSdkVersionMatch(android.os.Build.VERSION.SDK_INT >= 21)

                // 可选：设置文件存储路径
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

                // 可选：设置 SharedPreferences
                .setSharedPreferencesInvoker(new Function1<String, SharedPreferences>() {
                    @Override
                    public SharedPreferences invoke(String name) {
                        return application.getSharedPreferences("koom_prefs", android.content.Context.MODE_PRIVATE);
                    }
                })

                // 可选：设置 SharedPreferences keys 获取器
                .setSharedPreferencesKeysInvoker(new Function1<SharedPreferences, Set<String>>() {
                    @Override
                    public Set<String> invoke(SharedPreferences sp) {
                        return sp.getAll().keySet();
                    }
                })

                .build();
    }

    /**
     * 构建 OOMMonitorConfig
     */
    private OOMMonitorConfig buildMonitorConfig() {
        return new OOMMonitorConfig.Builder()
                // 线程阈值
                .setThreadThreshold(THREAD_THRESHOLD)
                // 文件描述符阈值
                .setFdThreshold(FD_THRESHOLD)
                // 堆内存阈值
                .setHeapThreshold(HEAP_THRESHOLD)
                // VSS 大小阈值
                .setVssSizeThreshold(VSS_SIZE_THRESHOLD)
                // 超过阈值的最大次数
                .setMaxOverThresholdCount(MAX_OVER_THRESHOLD_COUNT)
                // 每个版本最大分析次数
                .setAnalysisMaxTimesPerVersion(ANALYSIS_MAX_TIMES_PER_VERSION)
                // 每个版本分析周期
                .setAnalysisPeriodPerVersion((int) ANALYSIS_PERIOD_PER_VERSION)
                // 循环检测间隔
                .setLoopInterval(LOOP_INTERVAL)
                // 启用 hprof dump 分析
                .setEnableHprofDumpAnalysis(true)
                // 设置 hprof 上传器
                .setHprofUploader(createHprofUploader())
                // 设置报告上传器
                .setReportUploader(createReportUploader())
                .build();
    }

    /**
     * 创建 Hprof 文件上传器
     */
    private OOMHprofUploader createHprofUploader() {
        return new OOMHprofUploader() {
            @Override
            public void upload(File file, OOMHprofUploader.HprofType type) {
                MonitorLog.i(TAG, "=== Hprof 文件生成 ===");
                MonitorLog.i(TAG, "文件路径: " + file.getAbsolutePath());
                MonitorLog.i(TAG, "文件大小: " + file.length() / 1024 + " KB");
                MonitorLog.i(TAG, "文件类型: " + (type == OOMHprofUploader.HprofType.ORIGIN ? "原始文件" : "裁剪文件"));

                // TODO: 实现你的上传逻辑
                // 建议：
                // 1. 只在 WiFi 环境下上传
                // 2. 异步上传，避免阻塞主线程
                // 3. 可以压缩后再上传
                uploadToServer(file);
            }

            private void uploadToServer(File file) {
                // 示例：使用 OkHttp 上传
                // new Thread(() -> {
                //     try {
                //         // 上传文件到你的服务器
                //         // OkHttpClient client = new OkHttpClient();
                //         // RequestBody body = new MultipartBody.Builder()
                //         //         .setType(MultipartBody.FORM)
                //         //         .addFormDataPart("file", file.getName(),
                //         //                 RequestBody.create(MediaType.parse("application/octet-stream"), file))
                //         //         .build();
                //         // Request request = new Request.Builder()
                //         //         .url("https://your-server.com/upload/hprof")
                //         //         .post(body)
                //         //         .build();
                //         // client.newCall(request).execute();
                //     } catch (Exception e) {
                //         MonitorLog.e(TAG, "上传失败: " + e.getMessage());
                //     }
                // }).start();

                MonitorLog.i(TAG, "TODO: 实现 Hprof 文件上传逻辑");
            }
        };
    }

    /**
     * 创建报告上传器
     */
    private OOMReportUploader createReportUploader() {
        return new OOMReportUploader() {
            @Override
            public void upload(File file, String content) {
                MonitorLog.i(TAG, "=== 内存泄漏报告 ===");
                MonitorLog.i(TAG, "报告路径: " + file.getAbsolutePath());
                MonitorLog.i(TAG, "报告内容: " + content);

                // TODO: 实现你的上报逻辑
                // 建议：
                // 1. 上报到你的服务器
                // 2. 可以解析 content 中的泄漏信息
                // 3. 可以发送到崩溃分析平台（如 Bugly、Firebase）
                uploadReportToServer(file, content);
            }

            private void uploadReportToServer(File file, String content) {
                // 示例：解析泄漏信息
                try {
                    // content 是 JSON 格式，包含泄漏对象和引用链
                    // 示例：{"leakReason": "Activity泄漏", "leakObjects": [...]}
                    MonitorLog.i(TAG, "泄漏详情: " + content);

                    // 上报到服务器
                    // new Thread(() -> {
                    //     try {
                    //         OkHttpClient client = new OkHttpClient();
                    //         RequestBody body = RequestBody.create(
                    //                 MediaType.parse("application/json; charset=utf-8"),
                    //                 content
                    //         );
                    //         Request request = new Request.Builder()
                    //                 .url("https://your-server.com/upload/report")
                    //                 .post(body)
                    //                 .build();
                    //         client.newCall(request).execute();
                    //     } catch (Exception e) {
                    //         MonitorLog.e(TAG, "上报失败: " + e.getMessage());
                    //     }
                    // }).start();

                } catch (Exception e) {
                    MonitorLog.e(TAG, "解析报告失败: " + e.getMessage());
                }

                MonitorLog.i(TAG, "TODO: 实现报告上传逻辑");
            }
        };
    }
}