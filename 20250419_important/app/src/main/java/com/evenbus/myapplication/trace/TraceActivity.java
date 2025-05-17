package com.evenbus.myapplication.trace;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evenbus.myapplication.R;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class TraceActivity extends AppCompatActivity implements PerformanceAdapter.BindTimeListener {

        private PerformanceAdapter adapter;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private int frameDrops = 0;
        private long lastFrameTime = System.currentTimeMillis();
        private TextView performanceInfo;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_trace);

            performanceInfo = findViewById(R.id.performanceInfo);
            RecyclerView recyclerView = findViewById(R.id.recyclerView);

            // 初始化RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // 创建测试数据
            List<String> items = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                items.add(String.valueOf(i));
            }

            // 设置适配器
            adapter = new PerformanceAdapter(items, this);
            recyclerView.setAdapter(adapter);

            // 监听滚动性能
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    checkFrameRate();
                }
            });

            // 定期记录平均绑定时间
            handler.postDelayed(performanceLogger, 1000);
        }

        @Override
        public void onBindTime(long bindTime) {
            double avgTime = adapter.getAverageBindTime();
            performanceInfo.setText(String.format(Locale.getDefault(),
                    "最近一次绑定耗时: %dms\n平均绑定耗时: %.2fms\n掉帧次数: %d",
                    bindTime, avgTime, frameDrops));
        }

        private void checkFrameRate() {
            long currentTime = System.currentTimeMillis();
            long frameTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;

            // 如果帧时间超过16ms(60fps)，则认为掉帧
            if (frameTime > 16) {
                frameDrops++;
                Log.w("Performance", "Frame dropped! Frame time: " + frameTime + " ms");
            }
        }

        private final Runnable performanceLogger = new Runnable() {
            @Override
            public void run() {
                double avgTime = adapter.getAverageBindTime();
                Log.d("Performance", String.format(Locale.getDefault(),
                        "Average bind time: %.2fms, Frame drops: %d", avgTime, frameDrops));
                handler.postDelayed(this, 1000);
            }
        };

        @Override
        protected void onDestroy() {
            handler.removeCallbacks(performanceLogger);
            super.onDestroy();
        }
    }
