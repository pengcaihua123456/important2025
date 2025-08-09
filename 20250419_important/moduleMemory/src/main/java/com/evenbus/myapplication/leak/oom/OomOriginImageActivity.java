package com.evenbus.myapplication.leak.oom;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.module_memory.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * 演示内存溢出(OOM)的Activity，通过持续加载大图来消耗内存
 */
public class OomOriginImageActivity extends AppCompatActivity {
    // UI组件
    private ImageView mPhotoView;          // 显示图片的ImageView
    private TextView memoryInfoView;       // 显示内存信息的TextView

    // 内存管理相关
    private final List<Bitmap> bitmapHolder = new ArrayList<>(); // 强引用列表，用于持有Bitmap防止被回收

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat_photo);

        // 初始化视图
        mPhotoView = findViewById(R.id.photo_view);
        setupMemoryMonitor();  // 设置内存监控UI

        // 设置点击事件：在后台线程加载多张大图
        findViewById(R.id.tv_click).setOnClickListener(v -> {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    // 每次点击加载5张新图（可能触发OOM）
                    for(int i=0;i<5;i++){
                        loadNewImage();
                    }
                }
            }.start();
        });
    }

    /**
     * 加载新图片并消耗内存
     */
    private void loadNewImage() {
        try {
            // 1. 创建超大Bitmap（8000x6000 ARGB_8888格式，约192MB）
            Bitmap bitmap = Bitmap.createBitmap(8000, 6000, Bitmap.Config.ARGB_8888);

            // 2. 填充随机颜色（防止Bitmap复用优化）
            Canvas canvas = new Canvas(bitmap);
            int color = Color.rgb(
                    new Random().nextInt(256),
                    new Random().nextInt(256),
                    new Random().nextInt(256)
            );
            canvas.drawColor(color);

            // 3. 保存强引用（故意不释放内存）
            bitmapHolder.add(bitmap);
            // 更新UI显示最新图片
            runOnUiThread(() -> mPhotoView.setImageBitmap(bitmap));

            // 4. 更新内存信息
            updateMemoryInfo("已加载 " + bitmapHolder.size() + " 张图 (192MB/张)");

        } catch (OutOfMemoryError e) {
            // 捕获内存溢出异常
            updateMemoryInfo("OOM！最多加载: " + bitmapHolder.size() + " 张");
        }
    }

    /**
     * 初始化内存监控UI
     */
    private void setupMemoryMonitor() {
        // 创建显示内存信息的TextView
        memoryInfoView = new TextView(this);
        memoryInfoView.setTextColor(Color.RED);
        memoryInfoView.setBackgroundColor(Color.parseColor("#CCFFFFFF")); // 半透明白色背景

        // 设置布局参数（右下角显示）
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

        // 添加到PhotoView的父布局中
        ((ViewGroup) mPhotoView.getParent()).addView(memoryInfoView, params);

        updateMemoryInfo("点击按钮开始加载");
    }

    /**
     * 更新内存信息显示
     * @param status 当前状态文本
     */
    private void updateMemoryInfo(String status) {
        // 获取内存信息
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);
        long nativeMemory = memoryInfo.nativePss / 1024; // 转换为MB

        // 格式化显示信息
        String info = String.format(
                "%s\n当前Bitmaps: %d\nNative内存: %dMB",
                status,
                bitmapHolder.size(),
                nativeMemory
        );

        // 更新UI（确保在主线程执行）
        runOnUiThread(() -> memoryInfoView.setText(info));
    }
}