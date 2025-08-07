package com.evenbus.myapplication.leak.oom;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_memory.R;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class OomBigDataActivity extends AppCompatActivity {

        private static final String TAG = "VideoStreamDemo";
        private static final int VIDEO_SIZE_MB = 200;
        private static final int VIDEO_SIZE_BYTES = VIDEO_SIZE_MB * 1024 * 1024;
        private static final int CHUNK_SIZE = 2 * 1024 * 1024; // 2MB分块

        private Button btnDirectLoad, btnChunkLoad;
        private TextView tvStatus, tvMemoryInfo, tvResult;
        private ProgressBar progressBar;
        private Handler mainHandler;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_oom_bigdata);
            setTitle("加载抖音视频-内存分析");

            // 初始化视图
            btnDirectLoad = findViewById(R.id.btnDirectLoad);
            btnChunkLoad = findViewById(R.id.btnChunkLoad);
            tvStatus = findViewById(R.id.tvStatus);
            tvMemoryInfo = findViewById(R.id.tvMemoryInfo);
            tvResult = findViewById(R.id.tvResult);
            progressBar = findViewById(R.id.progressBar);
            mainHandler = new Handler(Looper.getMainLooper());

            // 更新内存信息
            updateMemoryInfo();

            // 直接加载按钮点击事件
            btnDirectLoad.setOnClickListener(v -> {
                tvStatus.setText("尝试直接加载500MB视频流...");
                tvResult.setText("");
                new DirectLoadTask(this).execute();
            });

            // 分块加载按钮点击事件
            btnChunkLoad.setOnClickListener(v -> {
                tvStatus.setText("开始分块加载500MB视频流...");
                tvResult.setText("");
                new ChunkLoadTask(this).execute();
            });
        }

        // 更新内存信息
        @SuppressLint("SetTextI18n")
        private void updateMemoryInfo() {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);

            // 获取堆内存信息
            long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
            long totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
            long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;

            String info = String.format(Locale.getDefault(),
                    "最大堆内存: %dMB\n总内存: %dMB\n已用内存: %dMB\n可用内存: %dMB\n500MB视频: %s",
                    maxMemory, totalMemory, usedMemory, freeMemory,
                    freeMemory > VIDEO_SIZE_MB ? "可能" : "不可能");

            tvMemoryInfo.setText("内存信息: " + info);
        }

        // 直接加载任务
        private static class DirectLoadTask extends AsyncTask<Void, Void, String> {
            private final WeakReference<OomBigDataActivity> activityRef;

            DirectLoadTask(OomBigDataActivity activity) {
                activityRef = new WeakReference<>(activity);
            }

            @Override
            protected void onPreExecute() {
                OomBigDataActivity activity = activityRef.get();
                if (activity != null) {
                    activity.progressBar.setVisibility(View.VISIBLE);
                    activity.progressBar.setIndeterminate(true);
                    activity.btnDirectLoad.setEnabled(false);
                    activity.btnChunkLoad.setEnabled(false);
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                OomBigDataActivity activity = activityRef.get();
                if (activity == null) return "Activity not available";

                try {
                    Log.d(TAG, "尝试分配500MB内存...");

                    // 尝试分配500MB内存
                    byte[] videoData = new byte[VIDEO_SIZE_BYTES];

                    // 模拟填充视频数据
                    for (int i = 0; i < VIDEO_SIZE_BYTES; i += 1024 * 1024) {
                        int blockSize = Math.min(1024 * 1024, VIDEO_SIZE_BYTES - i);
                        for (int j = 0; j < blockSize; j++) {
                            videoData[i + j] = (byte) (j % 256);
                        }
                    }

                    // 模拟处理视频数据
                    int checksum = 0;
                    for (int i = 0; i < VIDEO_SIZE_BYTES; i += 1024 * 1024) {
                        checksum += videoData[i];
                    }

                    return "直接加载成功! 视频大小: " + VIDEO_SIZE_MB + "MB, 校验和: " + checksum;
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "内存溢出错误: " + e.getMessage());
                    return "内存溢出错误: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                OomBigDataActivity activity = activityRef.get();
                if (activity != null) {
                    activity.progressBar.setIndeterminate(false);
                    activity.progressBar.setVisibility(View.GONE);
                    activity.tvStatus.setText("加载完成");
                    activity.tvResult.setText(result);
                    activity.tvResult.setTextColor(result.startsWith("直接加载成功") ?
                            0xFF4CAF50 : 0xFFF44336);
                    activity.btnDirectLoad.setEnabled(true);
                    activity.btnChunkLoad.setEnabled(true);
                    activity.updateMemoryInfo();
                }
            }
        }

        // 分块加载任务
        private static class ChunkLoadTask extends AsyncTask<Void, Integer, String> {
            private final WeakReference<OomBigDataActivity> activityRef;

            ChunkLoadTask(OomBigDataActivity activity) {
                activityRef = new WeakReference<>(activity);
            }

            @Override
            protected void onPreExecute() {
                OomBigDataActivity activity = activityRef.get();
                if (activity != null) {
                    activity.progressBar.setVisibility(View.VISIBLE);
                    activity.progressBar.setProgress(0);
                    activity.btnDirectLoad.setEnabled(false);
                    activity.btnChunkLoad.setEnabled(false);
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                OomBigDataActivity activity = activityRef.get();
                if (activity == null) return "Activity not available";

                try {
                    int chunks = VIDEO_SIZE_BYTES / CHUNK_SIZE;
                    int checksum = 0;

                    for (int i = 0; i < chunks; i++) {
                        // 检查是否取消
                        if (isCancelled()) {
                            return "加载已取消";
                        }

                        // 分配小块内存
                        byte[] chunk = new byte[CHUNK_SIZE];

                        // 填充模拟数据
                        for (int j = 0; j < CHUNK_SIZE; j++) {
                            chunk[j] = (byte) ((i * CHUNK_SIZE + j) % 256);
                        }

                        // 处理数据块
                        for (int j = 0; j < CHUNK_SIZE; j += 1024) {
                            checksum += chunk[j];
                        }

                        // 释放引用，允许垃圾回收
                        chunk = null;
                        System.gc();

                        // 更新进度
                        publishProgress((i * 100) / chunks);
                    }

                    return "分块加载成功! 处理了" + VIDEO_SIZE_MB + "MB, 校验和: " + checksum;
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "内存溢出错误: " + e.getMessage());
                    return "内存溢出错误: " + e.getMessage();
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                OomBigDataActivity activity = activityRef.get();
                if (activity != null) {
                    activity.progressBar.setProgress(values[0]);
                    activity.tvStatus.setText("分块加载中: " + values[0] + "%");
                }
            }

            @Override
            protected void onPostExecute(String result) {
                OomBigDataActivity activity = activityRef.get();
                if (activity != null) {
                    activity.progressBar.setVisibility(View.GONE);
                    activity.tvStatus.setText("加载完成");
                    activity.tvResult.setText(result);
                    activity.tvResult.setTextColor(0xFF4CAF50);
                    activity.btnDirectLoad.setEnabled(true);
                    activity.btnChunkLoad.setEnabled(true);
                    activity.updateMemoryInfo();

                    if (!result.startsWith("分块加载成功")) {
                        Toast.makeText(activity, "加载过程中出现问题", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }