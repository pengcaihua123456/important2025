package com.evenbus.view.finger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 自定义View实现指纹识别动画效果：
 * 1. 用户触摸时显示中心大圆环和指纹图标
 * 2. 延迟后触发24个红色小圆点动画：
 *    - 从大圆环边缘向外扩散
 *    - 伴随上浮、水平漂移、放大和淡出效果
 * 3. 使用缓动函数实现平滑动画
 */
public class FingerprintAnimationView extends View {
    private static final String TAG = "FingerprintAnimationView";

    // 尺寸常量
    private static final int BASE_CIRCLE_RADIUS = 160;  // 大圆环半径（像素）
    private static final int FINGERPRINT_ICON_SIZE = 50; // 指纹图标尺寸（像素）
    private static final int SMALL_CIRCLE_COUNT = 24;   // 小圆点数量
    private static final int ANIMATION_DURATION = 2000; // 动画总时长（毫秒）
    private static final int DELAY_BEFORE_ANIMATION = 400; // 动画开始前延迟（毫秒）

    // 绘图工具
    private Paint baseCirclePaint;       // 大圆环画笔（描边样式）
    private Paint smallCirclePaint;      // 小圆点画笔（填充样式）
    private Bitmap fingerprintIcon;      // 指纹图标位图

    // 动画状态控制
    private PointF touchPoint;           // 触摸点坐标（动画中心）
    private boolean isTouching = false;  // 当前触摸状态标志
    private long touchStartTime;         // 触摸开始时间戳
    private List<SmallCircle> smallCircles = new ArrayList<>(); // 小圆点对象集合
    private Random random = new Random(); // 随机数生成器（用于小圆点参数）

    // 构造方法
    public FingerprintAnimationView(Context context) {
        super(context);
        init();
    }

    public FingerprintAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化View：
     * 1. 配置大圆环画笔（白色描边）
     * 2. 配置小圆点画笔（红色填充）
     * 3. 生成指纹图标位图
     */
    private void init() {
        // 大圆环画笔设置（半透明描边）
        baseCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baseCirclePaint.setStyle(Paint.Style.STROKE);
        baseCirclePaint.setStrokeWidth(4);
        baseCirclePaint.setColor(Color.argb(200, 255, 255, 255)); // 半透明白色

        // 小圆点画笔设置（纯红色填充）
        smallCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallCirclePaint.setStyle(Paint.Style.FILL);
        smallCirclePaint.setColor(Color.RED); // 纯红色

        // 创建指纹图标（白色线条组成的简易图标）
        fingerprintIcon = createFingerprintIcon();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 仅在触摸状态下绘制
        if (isTouching && touchPoint != null) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - touchStartTime; // 计算已耗时

            // 1. 绘制大圆环和中心指纹图标
            canvas.drawCircle(touchPoint.x, touchPoint.y, BASE_CIRCLE_RADIUS, baseCirclePaint);
            canvas.drawBitmap(
                    fingerprintIcon,
                    touchPoint.x - FINGERPRINT_ICON_SIZE / 2f, // 居中对齐
                    touchPoint.y - FINGERPRINT_ICON_SIZE / 2f,
                    null
            );

            // 2. 延迟结束后开始小圆点动画
            if (elapsedTime > DELAY_BEFORE_ANIMATION) {
                // 计算动画进度（0.0~1.0）
                float animationProgress = (elapsedTime - DELAY_BEFORE_ANIMATION) / (float) ANIMATION_DURATION;
                animationProgress = Math.min(animationProgress, 1.0f); // 限制最大值

                // 更新所有小圆点状态
                for (SmallCircle circle : smallCircles) {
                    updateCirclePosition(circle, animationProgress);
                }
            }

            // 3. 绘制所有小圆点（动态更新透明度）
            for (SmallCircle circle : smallCircles) {
                smallCirclePaint.setAlpha(circle.currentAlpha); // 设置实时透明度
                canvas.drawCircle(
                        circle.currentX,
                        circle.currentY,
                        circle.currentRadius,
                        smallCirclePaint
                );
            }

            // 4. 动画未完成时继续刷新，否则结束触摸状态
            if (elapsedTime < DELAY_BEFORE_ANIMATION + ANIMATION_DURATION) {
                postInvalidateOnAnimation(); // 请求下一帧动画
            } else {
                isTouching = false; // 动画完成
            }
        }
    }

    /**
     * 更新小圆点状态（基于动画进度）
     * @param circle 目标圆点对象
     * @param progress 线性进度（0.0~1.0）
     */
    private void updateCirclePosition(SmallCircle circle, float progress) {
        // 应用缓动函数（先快后慢）
        float animatedProgress = easeOutQuad(progress);

        // 位置变化：垂直上浮 + 水平随机漂移
        circle.currentY = circle.startY - 300 * animatedProgress; // 上浮300像素
        circle.currentX = circle.startX + (circle.xDrift * animatedProgress * 100); // 水平漂移

        // 大小变化：半径增大50%
        circle.currentRadius = circle.baseRadius * (1 + animatedProgress * 0.5f);

        // 透明度变化：完全淡出
        circle.currentAlpha = (int) (circle.baseAlpha * (1 - animatedProgress));
    }

    /**
     * 处理触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(event.getX(), event.getY()); // 处理按下事件
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouching = false; // 结束触摸状态
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 处理触摸按下事件：
     * 1. 记录触摸点坐标
     * 2. 初始化动画状态
     * 3. 在圆环上均匀生成24个小圆点
     * @param x 触摸点X坐标
     * @param y 触摸点Y坐标
     */
    private void handleTouchDown(float x, float y) {
        Log.d(TAG, "handleTouchDown start");

        // 初始化触摸状态
        touchPoint = new PointF(x, y);
        isTouching = true;
        touchStartTime = System.currentTimeMillis();
        smallCircles.clear(); // 清空旧圆点

        // 在圆环上均匀生成小圆点
        float angleStep = (float) (2 * Math.PI / SMALL_CIRCLE_COUNT); // 角度间隔
        for (int i = 0; i < SMALL_CIRCLE_COUNT; i++) {
            SmallCircle circle = new SmallCircle();
            float angle = angleStep * i; // 当前角度

            // 计算圆环上的起始位置
            circle.startX = x + (float) Math.cos(angle) * BASE_CIRCLE_RADIUS;
            circle.startY = y + (float) Math.sin(angle) * BASE_CIRCLE_RADIUS;

            // 初始化实时位置
            circle.currentX = circle.startX;
            circle.currentY = circle.startY;

            // 设置随机参数
            circle.baseRadius = 6 + random.nextFloat() * 4;  // 半径6~10像素
            circle.currentRadius = circle.baseRadius;
            circle.baseAlpha = 220 + random.nextInt(35);    // 透明度220~255
            circle.currentAlpha = circle.baseAlpha;
            circle.xDrift = (random.nextFloat() - 0.5f) * 2f; // 水平漂移方向（-1~1）

            smallCircles.add(circle);
        }

        Log.d(TAG, "handleTouchDown end");
        invalidate(); // 触发首次绘制
    }

    /**
     * 缓动函数：二次方缓出（先快后慢）
     * @param t 输入进度（0.0~1.0）
     * @return 应用缓动后的进度
     */
    private float easeOutQuad(float t) {
        return t * (2 - t);
    }

    /**
     * 创建指纹图标（简易矢量图形）
     * @return 生成的位图对象
     */
    private Bitmap createFingerprintIcon() {
        // 创建透明位图
        Bitmap bitmap = Bitmap.createBitmap(
                FINGERPRINT_ICON_SIZE,
                FINGERPRINT_ICON_SIZE,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE); // 白色线条
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        // 绘制指纹特征：两个圆点和一条竖线
        canvas.drawCircle(FINGERPRINT_ICON_SIZE/2f, FINGERPRINT_ICON_SIZE/3f, 5, paint); // 上圆点
        canvas.drawCircle(FINGERPRINT_ICON_SIZE/2f, FINGERPRINT_ICON_SIZE*2f/3f, 5, paint); // 下圆点
        canvas.drawLine(
                FINGERPRINT_ICON_SIZE/2f, 5,
                FINGERPRINT_ICON_SIZE/2f, FINGERPRINT_ICON_SIZE-5,
                paint
        ); // 中央竖线

        return bitmap;
    }

    /**
     * 小圆点数据容器
     */
    private static class SmallCircle {
        float startX, startY;     // 起始位置（圆环上的点）
        float currentX, currentY; // 实时位置
        float baseRadius;         // 初始半径
        float currentRadius;      // 实时半径
        int baseAlpha;            // 初始透明度
        int currentAlpha;         // 实时透明度
        float xDrift;             // 水平漂移系数（决定左右方向）
    }
}
