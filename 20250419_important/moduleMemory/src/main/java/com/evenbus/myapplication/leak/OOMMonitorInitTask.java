package com.evenbus.myapplication.leak;

import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.kwai.koom.base.CommonConfig;
import com.kwai.koom.base.InitTask;
import com.kwai.koom.javaoom.monitor.OOMHprofUploader;
import com.kwai.koom.javaoom.monitor.OOMMonitor;
import com.kwai.koom.javaoom.monitor.OOMMonitorConfig;
import com.kwai.koom.javaoom.monitor.OOMReportUploader;

import java.io.File;

import kotlin.jvm.functions.Function0;


/***
 * OOMMonitor 是tag
 *
 * oom meminfo.rate < 5.0%
 *
 * koom产生报告，也不一定代表有问题吧！如果你设置的阈值比较小的话！
 * 如果设置比较大，就是有问题的
 *
 * OOMMonitorInitTask 中把监控阈值设置得很低（60% 就报警，正常是 80% 才报警
 */
public class OOMMonitorInitTask implements InitTask {

    private static final String TAG = "OOMMonitorInitTask";

    private static volatile OOMMonitorInitTask instance;

    private OOMMonitorInitTask() {
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

    public void start() {
        init(DefaultInitTask.mApplication);
    }

    @Override
    public void init(Application application) {
        Log.i(TAG, "========== KOOM 初始化开始 ==========");
        Log.i(TAG, "进程名: " + getProcessName(application));

        try {
            // 1. 确保 DefaultInitTask 已经执行
            if (DefaultInitTask.mApplication == null) {
                Log.i(TAG, "DefaultInitTask 未执行，手动执行");
                DefaultInitTask.init(application);
            }

            // 2. 获取 CommonConfig
            CommonConfig commonConfig = DefaultInitTask.sCommonConfig;
            if (commonConfig == null) {
                Log.e(TAG, "sCommonConfig 为 null！重新构建...");
                commonConfig = new CommonConfig.Builder()
                        .setApplication(application)
                        .setDebugMode(true)
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
                        .setSdkVersionMatch(
                                Build.VERSION.SDK_INT <= 34 &&
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        )
                        .build();
                DefaultInitTask.sCommonConfig = commonConfig;
            }

            // 3. 构建 OOMMonitorConfig（使用低阈值便于测试）
            OOMMonitorConfig config = new OOMMonitorConfig.Builder()
                    // ========== 测试配置（低阈值，容易触发）==========
                    .setThreadThreshold(50)                    // 线程阈值
                    .setFdThreshold(300)                       // 文件描述符阈值
                    .setHeapThreshold(30f)                    // 【测试】堆内存阈值 30%，正常是 0.8
                    .setVssSizeThreshold(1_000_000)            // VSS 阈值
                    .setMaxOverThresholdCount(1)               // 【测试】超过阈值 1 次就触发，正常是 3
                    .setLoopInterval(5000L)                    // 【测试】每 5 秒检测一次，正常是 30 秒
                    // ========== 分析配置 ==========
                    .setAnalysisMaxTimesPerVersion(3)
                    .setAnalysisPeriodPerVersion((int) (15 * 24 * 60 * 60 * 1000L))
                    .setEnableHprofDumpAnalysis(true)
                    .setHprofUploader(new OOMHprofUploader() {
                        @Override
                        public void upload(File file, OOMHprofUploader.HprofType type) {
                            Log.i(TAG, "========================================");
                            Log.i(TAG, "🔴 Hprof 文件已生成！");
                            Log.i(TAG, "文件路径: " + file.getAbsolutePath());
                            Log.i(TAG, "文件大小: " + file.length() / 1024 + " KB");
                            Log.i(TAG, "文件类型: " + (type == OOMHprofUploader.HprofType.ORIGIN ? "原始文件" : "裁剪文件"));
                            Log.i(TAG, "========================================");
                            // TODO: 上传到服务器
                        }
                    })
                    .setReportUploader(new OOMReportUploader() {
                        @Override
                        public void upload(File file, String content) {
                            Log.i(TAG, "========================================");
                            Log.i(TAG, "🔴 内存泄漏报告！");
                            Log.i(TAG, "报告路径: " + file.getAbsolutePath());
                            Log.i(TAG, "报告内容: " + content);
                            Log.i(TAG, "========================================");
                            // TODO: 上传报告到服务器
                        }
                    })
                    .build();

            // 4. 初始化 OOMMonitor
            OOMMonitor.INSTANCE.init(commonConfig, config);
            Log.i(TAG, "OOMMonitor.init() 完成");

            // 5. 启动监控循环
            OOMMonitor.INSTANCE.startLoop(true, false, 5000L);
            Log.i(TAG, "========== KOOM 监控已启动 ==========");
            Log.i(TAG, "检测配置: 堆内存阈值=30%, 连续超过阈值次数=1, 检测间隔=5秒");

        } catch (Exception e) {
            Log.e(TAG, "KOOM 初始化失败！", e);
            e.printStackTrace();
        }
    }

    private String getProcessName(Application application) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            return Application.getProcessName();
        }
        try {
            int pid = android.os.Process.myPid();
            ActivityManager am = (ActivityManager) application.getSystemService(android.content.Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == pid) {
                    return info.processName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }
}