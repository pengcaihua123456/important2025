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

/**
 * 卡顿动画演示Activity
 * 该类专门用于演示和测试由于过度绘制和频繁UI更新导致的卡顿现象
 * 注意：此代码 intentionally 设计为低效，用于性能测试目的
 */
public class LaggyAnimationActivity extends AppCompatActivity {

    // 主动画视图引用
    private View animationView;

    // 存储所有子视图的列表 - 使用ArrayList可能导致内存和性能问题
    private List<View> views = new ArrayList<>();

    /**
     * Activity创建时的回调方法
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局文件
        setContentView(R.layout.activity_laggy_animation);

        setTitle("复制动画导致的卡顿");

        // 获取主动画视图
        animationView = findViewById(R.id.animation_view);

        // 获取容器视图用于添加子视图
        ViewGroup container = findViewById(R.id.container);

        // 创建大量视图 - 这是导致卡顿的主要原因之一
        // 问题1: 创建10000个视图远超Android推荐的最佳实践
        // 问题2: 每个视图都需要内存分配和初始化，消耗大量资源
        for (int i = 0; i < 10000; i++) {
            View view = new View(this);
            // 设置视图布局参数
            view.setLayoutParams(new ViewGroup.LayoutParams(20, 20));
            // 设置视图背景颜色
            view.setBackgroundColor(Color.RED);
            // 将视图添加到容器
            container.addView(view);
            // 将视图添加到列表以便后续操作
            views.add(view);
        }

        // 启动复杂的动画 - 这会进一步加剧卡顿
        startLaggyAnimation();
    }

    /**
     * 启动导致卡顿的动画
     * 这个方法创建了一个复杂的路径动画，并在每帧更新大量视图
     */
    private void startLaggyAnimation() {
        // 创建复杂的运动路径
        // 问题3: 路径过于复杂，包含50个线段
        Path path = new Path();
        path.moveTo(0, 0);
        for (int i = 1; i <= 50; i++) {
            // 添加随机高度的线段，增加计算复杂度
            path.lineTo(i * 20, (float) (Math.random() * 500));
        }

        // 使用PathMeasure来计算路径上的位置
        final PathMeasure pathMeasure = new PathMeasure(path, false);
        final float pathLength = pathMeasure.getLength();

        // 创建值动画器
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        // 设置动画持续时间
        animator.setDuration(5000);
        // 设置无限循环
        animator.setRepeatCount(ValueAnimator.INFINITE);
        // 使用线性插值器
        animator.setInterpolator(new LinearInterpolator());

        // 添加动画更新监听器
        // 问题4: 在每帧动画更新时执行大量操作
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 将动画更新逻辑提取到单独方法中
                updateAnimation(animation, pathLength, pathMeasure);
            }
        });

        // 启动动画
        animator.start();
    }

    /**
     * 更新动画状态 - 这是卡顿的主要来源
     * 每帧都会调用此方法，执行大量计算和UI更新
     *
     * @param animation 动画实例
     * @param pathLength 路径总长度
     * @param pathMeasure 路径测量工具
     */
    private void updateAnimation(ValueAnimator animation, float pathLength, PathMeasure pathMeasure) {
        // 获取当前动画进度（0到1之间）
        float fraction = (float) animation.getAnimatedValue();

        // 计算当前在路径上的距离
        float distance = fraction * pathLength;

        // 存储位置信息的数组
        float[] pos = new float[2];

        // 获取路径上指定距离的位置
        pathMeasure.getPosTan(distance, pos, null);

        // 更新主动画视图的位置
        animationView.setTranslationX(pos[0]);
        animationView.setTranslationY(pos[1]);

        // 问题5: 在每帧更新10000个子视图 - 这是性能杀手！
        // 每次循环都涉及：
        // - 列表查找（views.get(i)）
        // - 多个属性设置（setTranslationX, setTranslationY等）
        // - 数学计算（i * 5, fraction * 360 * 5等）
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);

            // 设置X轴平移 - 基于主视图位置和索引偏移
            view.setTranslationX(pos[0] + i * 5);

            // 设置Y轴平移 - 基于主视图位置和索引偏移
            view.setTranslationY(pos[1] + i * 5);

            // 设置旋转 - 基于动画进度和索引
            // 问题6: 复杂的数学计算在每帧执行10000次
            view.setRotation(fraction * 360 * 5);

            // 设置X轴缩放 - 基于动画进度
            view.setScaleX(0.5f + fraction);

            // 设置Y轴缩放 - 基于动画进度
            view.setScaleY(0.5f + fraction);
        }

        // 性能问题总结：
        // 1. 每帧执行50000次属性设置（10000视图 × 5个属性）
        // 2. 每帧执行10000次列表查找
        // 3. 每帧执行大量数学计算
        // 4. 导致频繁的UI线程阻塞和重绘
    }

    /**
     * Activity销毁时的清理工作
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源，避免内存泄漏
        if (views != null) {
            views.clear();
        }
    }

    /**
     * 获取当前活动的视图数量
     * @return 视图数量
     */
    public int getViewCount() {
        return views != null ? views.size() : 0;
    }

    /**
     * 停止所有动画（如果需要的话）
     * 这个方法可以扩展来实现动画停止功能
     */
    public void stopAnimations() {
        // 实现动画停止逻辑
        // 注意：当前实现中没有保存animator引用，需要修改才能支持停止功能
    }
}