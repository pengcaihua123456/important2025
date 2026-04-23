package com.evenbus.myapplication.leak.oom;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
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

        Log.d("OomOriginImageActivity","onCreate");

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
                    for(int i=0;i<10;i++){
                        loadNewImage();
                    }
                }
            }.start();
        });
    }

    /**
     * 加载新图片并消耗内存
     */
    // 控制变量
    private boolean useHalfSize = true;  // true:使用一半尺寸，false:使用原始尺寸

    private void loadNewImage() {
        try {
            // 原始尺寸
            int originalWidth = 8000;
            int originalHeight = 6000;

            int width, height;

            // 一键切换
            if (useHalfSize) {
                // 使用一半尺寸
                width = (int)(originalWidth / 1.5);   // 4000
                height = (int) (originalHeight / 1.5); // 3000
            } else {
                // 使用原始尺寸
                width = originalWidth;   // 8000
                height = originalHeight; // 6000
            }

            // 计算内存大小
            long memoryMB = (width * height * 4L) / (1024 * 1024);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            int color = Color.rgb(
                    new Random().nextInt(256),
                    new Random().nextInt(256),
                    new Random().nextInt(256)
            );
            canvas.drawColor(color);

            bitmapHolder.add(bitmap);
            runOnUiThread(() -> mPhotoView.setImageBitmap(bitmap));

            // 显示当前使用的尺寸
            String sizeMode = useHalfSize ? "一半尺寸" : "原始尺寸";
            updateMemoryInfo(sizeMode + ": " + width + "x" + height +
                    " (" + memoryMB + "MB/张) 共" + bitmapHolder.size() + "张");

        } catch (OutOfMemoryError e) {
            String sizeMode = useHalfSize ? "一半尺寸" : "原始尺寸";
            updateMemoryInfo(sizeMode + " OOM！已加载: " + bitmapHolder.size() + " 张");
        }
    }

    // 一键切换方法（点击按钮时调用）
    public void toggleImageSize() {
        useHalfSize = !useHalfSize;  // 切换状态
        loadNewImage();               // 重新加载
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