package com.evenbus.myapplication.trace;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 极端卡顿动画演示Activity - 演示大量视图和复杂变换导致的严重性能问题
 * 注意：此代码专门设计为制造严重卡顿，用于性能测试目的
 */
public class LaggyAnimationActivity extends AppCompatActivity {

    private View animationView;
    private List<View> views = new ArrayList<>();
    private ValueAnimator animator;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laggy_animation);
        setTitle("复杂变换导致的严重卡顿");

        animationView = findViewById(R.id.animation_view);

        ViewGroup container = findViewById(R.id.container);

        // 创建大量视图 - 10000个视图
        for (int i = 0; i < 10000; i++) {
            View view = new View(this);
            view.setLayoutParams(new ViewGroup.LayoutParams(20, 20));

            // 使用随机颜色
            int color = Color.argb(200,
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256));
            view.setBackgroundColor(color);

            container.addView(view);
            views.add(view);
        }

        // 启动极端卡顿动画
        startExtremeLaggyAnimation();
    }

    private void startExtremeLaggyAnimation() {
        // 创建复杂的运动路径
        Path path = new Path();
        path.moveTo(0, 0);
        for (int i = 1; i <= 50; i++) {
            path.lineTo(i * 20, (float) (Math.sin(i * 0.2) * 200 + Math.cos(i * 0.1) * 100));
        }

        final PathMeasure pathMeasure = new PathMeasure(path, false);
        final float pathLength = pathMeasure.getLength();

        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(4000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(animation -> {
            updateExtremeAnimation(animation, pathLength, pathMeasure);
        });

        animator.start();
    }

    private void updateExtremeAnimation(ValueAnimator animation, float pathLength, PathMeasure pathMeasure) {
        float fraction = (float) animation.getAnimatedValue();
        float distance = fraction * pathLength;
        float[] pos = new float[2];
        pathMeasure.getPosTan(distance, pos, null);

        // 更新主动画视图
        animationView.setTranslationX(pos[0]);
        animationView.setTranslationY(pos[1]);

        // 极端复杂的视图变换 - 每帧更新10000个视图的多个属性
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);

            // 1. 复杂的波浪形移动
            float waveOffsetX = (float) Math.sin(i * 0.01f + fraction * Math.PI * 4) * 60;
            float waveOffsetY = (float) Math.cos(i * 0.008f + fraction * Math.PI * 3) * 40;
            float circularX = (float) Math.cos(i * 0.005f + fraction * Math.PI * 2) * 50;
            float circularY = (float) Math.sin(i * 0.005f + fraction * Math.PI * 2) * 30;

            view.setTranslationX(pos[0] + i * 4 + waveOffsetX + circularX);
            view.setTranslationY(pos[1] + i * 3 + waveOffsetY + circularY);

            // 2. 复杂的多层旋转
            float baseRotation = fraction * 360 * 6; // 基础快速旋转
            float waveRotation = (float) Math.sin(i * 0.015f + fraction * Math.PI * 5) * 120; // 正弦波动
            float indexRotation = i * 0.3f; // 基于索引的旋转

            view.setRotation(baseRotation + waveRotation + indexRotation);

            // 3. 复杂的脉冲缩放效果
            float pulse = (float) (0.4f + Math.sin(fraction * Math.PI * 8 + i * 0.012f) * 0.3f);
            float scaleVariationX = (float) Math.cos(i * 0.004f + fraction * Math.PI) * 0.15f;
            float scaleVariationY = (float) Math.sin(i * 0.004f + fraction * Math.PI) * 0.15f;

            view.setScaleX(pulse + scaleVariationX);
            view.setScaleY(pulse + scaleVariationY);

            // 4. 复杂的波浪式透明度变化
            float alphaWave1 = (float) Math.sin(fraction * Math.PI * 3 + i * 0.01f) * 0.4f;
            float alphaWave2 = (float) Math.cos(fraction * Math.PI * 2 + i * 0.008f) * 0.3f;
            float baseAlpha = 0.3f;

            float alpha = baseAlpha + alphaWave1 + alphaWave2;
            view.setAlpha(Math.max(0.1f, Math.min(1.0f, alpha)));

            // 5. 动态颜色变化（每50个视图更新一次）
            if (i % 50 == 0) {
                int red = (int) ((Math.sin(fraction * Math.PI * 2 + i * 0.001f) * 0.5f + 0.5f) * 255);
                int green = (int) ((Math.cos(fraction * Math.PI * 3 + i * 0.002f) * 0.5f + 0.5f) * 255);
                int blue = (int) ((Math.sin(fraction * Math.PI * 4 + i * 0.003f) * 0.5f + 0.5f) * 255);

                int color = Color.argb(200, red, green, blue);
                view.setBackgroundColor(color);
            }
        }

        // 每帧执行重型计算来加剧卡顿
        performHeavyFrameCalculations();
    }

    /**
     * 每帧执行的重型数学计算 - 阻塞UI线程
     */
    private void performHeavyFrameCalculations() {
        double result = 0;
        // 复杂的数学计算
        for (int j = 0; j < 15000; j++) {
            // 多种三角函数和数学运算组合
            double angle = j * 0.1;
            result += Math.sin(angle) * Math.cos(angle * 2)
                    * Math.tan(angle * 0.5 + 0.1)
                    * Math.log(j + 2);

            // 添加条件判断增加计算复杂度
            if (j % 500 == 0) {
                result *= Math.sqrt(j + 1);
            }
        }

        // 创建临时对象触发GC
        if (System.currentTimeMillis() % 1000 < 16) { // 大约每帧一次
            List<String> tempObjects = new ArrayList<>();
            for (int k = 0; k < 50; k++) {
                tempObjects.add("calculation_temp_" + k + "_" + result);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel();
        }
        if (views != null) {
            views.clear();
        }
    }

    /**
     * 获取当前视图数量
     */
    public int getViewCount() {
        return views != null ? views.size() : 0;
    }

    /**
     * 停止动画
     */
    public void stopAnimation() {
        if (animator != null) {
            animator.cancel();
        }
    }
}