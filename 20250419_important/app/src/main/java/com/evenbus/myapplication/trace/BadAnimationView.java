package com.evenbus.myapplication.trace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * 极端错误动画演示View - 展示疯狂频繁invalidate()导致的严重卡顿问题
 * 警告：此代码仅用于性能问题演示，绝对不要在生产环境中使用！
 */
public class BadAnimationView extends View {
    private Paint paint;
    private Random random;
    private boolean isAnimating = false;

    // 动画参数
    private int circleX = 0;
    private int circleY = 0;
    private int circleRadius = 30;

    private float dx = 5f;
    private float dy = 3;

    // 性能统计
    private int invalidateCount = 0;
    private long lastFrameTime = 0;
    private int frameCount = 0;
    private int currentFPS = 0;

    // 回调接口
    private OnAnimationUpdateListener updateListener;

    // 多个Handler制造更严重卡顿
    private Handler handler1;
    private Handler handler2;
    private Handler handler3;
    private boolean mOnDrawInvalidateMode = false;

    public BadAnimationView(Context context) {
        super(context);
        init();
    }

    public BadAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        random = new Random();
        handler1 = new Handler(Looper.getMainLooper());
        handler2 = new Handler(Looper.getMainLooper());
        handler3 = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        circleX = w / 2;
        circleY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 计算FPS
        calculateFPS();

        // 绘制背景
        canvas.drawColor(Color.parseColor("#f8f8f8"));

        // 绘制 bouncing ball
        canvas.drawCircle(circleX, circleY, circleRadius, paint);

        // 绘制轨迹
        drawTrail(canvas);

        // 绘制性能信息
        drawPerformanceInfo(canvas);

        // 极端模式：在onDraw中也疯狂调用invalidate - 死亡循环！
        if (mOnDrawInvalidateMode && isAnimating) {
            // 一次onDraw中调用多次invalidate！
            for (int i = 0; i < 3; i++) {
                invalidateCount++;
                if (updateListener != null) {
                    updateListener.onInvalidateCalled(invalidateCount, currentFPS);
                }
                invalidate(); // 在渲染过程中又请求重绘
            }

            // 添加即时物理更新
            updatePhysics();
            updatePhysics(); // 更新两次！
        }
    }

    private void calculateFPS() {
        long currentTime = System.currentTimeMillis();
        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
        }

        frameCount++;
        long elapsedTime = currentTime - lastFrameTime;

        if (elapsedTime >= 1000) {
            currentFPS = (int) (frameCount * 1000 / elapsedTime);
            frameCount = 0;
            lastFrameTime = currentTime;

            if (updateListener != null) {
                updateListener.onFPSUpdated(currentFPS);
            }
        }
    }

    private void drawTrail(Canvas canvas) {
        Paint trailPaint = new Paint();
        trailPaint.setColor(Color.argb(64, 255, 0, 0));
        trailPaint.setStyle(Paint.Style.FILL);

        // 绘制更复杂的轨迹
        for (int i = 0; i < 8; i++) { // 从5个增加到8个轨迹
            float trailX = circleX - dx * i * 2;
            float trailY = circleY - dy * i * 2;
            int trailRadius = circleRadius - i * 2; // 更缓慢的缩小
            if (trailRadius > 0) {
                canvas.drawCircle(trailX, trailY, trailRadius, trailPaint);
            }
        }
    }

    private void drawPerformanceInfo(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24);
        textPaint.setAntiAlias(true);

        String info = "FPS: " + currentFPS + " | Invalidates: " + invalidateCount + " | 极端卡顿模式";
        canvas.drawText(info, 10, 30, textPaint);

        // 添加额外的性能信息显示
        textPaint.setTextSize(18);
        canvas.drawText("警告：极度频繁invalidate调用！", 10, 60, textPaint);
    }

    /**
     * 启动极端错误动画 - 制造严重卡顿
     */
    public void startExtremeBadAnimation() {
        if (isAnimating) return;

        isAnimating = true;
        invalidateCount = 0;
        frameCount = 0;
        lastFrameTime = 0;

        // 重置位置
        circleX = getWidth() / 2;
        circleY = getHeight() / 2;

        // 更快的速度增加卡顿感
        dx = random.nextInt(15) + 10; // 10-24的速度（原来3-9）
        dy = random.nextInt(10) + 8;  // 8-17的速度（原来2-6）
        if (random.nextBoolean()) dx = -dx;
        if (random.nextBoolean()) dy = -dy;

        // 启用onDraw中的invalidate调用
        mOnDrawInvalidateMode = true;

        // 第一个Handler：极度频繁的invalidate调用
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAnimating) return;

                // 极端频繁的物理更新
                for (int i = 0; i < 5; i++) {
                    updatePhysics();
                }

                // 疯狂调用invalidate
                invalidateCount += 3;
                if (updateListener != null) {
                    updateListener.onInvalidateCalled(invalidateCount, currentFPS);
                }

                invalidate();
                postInvalidate();
                invalidate();

                // 极短的延迟
                handler1.postDelayed(this, 0);
            }
        }, 0);

        // 第二个Handler：额外的重绘压力
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAnimating) return;

                // 添加额外的无效重绘
                invalidateCount++;
                if (updateListener != null) {
                    updateListener.onInvalidateCalled(invalidateCount, currentFPS);
                }

                postInvalidate();

                // 重型计算阻塞UI线程
                performHeavyCalculation();

                handler2.postDelayed(this, 1);
            }
        }, 0);

        // 第三个Handler：制造UI线程阻塞
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAnimating) return;

                // 在UI线程执行重型计算
                performHeavyCalculation();

                // 再次调用invalidate
                invalidateCount++;
                if (updateListener != null) {
                    updateListener.onInvalidateCalled(invalidateCount, currentFPS);
                }
                invalidate();

                handler3.postDelayed(this, 2);
            }
        }, 0);
    }

    /**
     * 重型计算阻塞UI线程
     */
    private void performHeavyCalculation() {
        // 制造严重的UI线程阻塞
        double result = 0;
        for (int i = 0; i < 300000; i++) {
            result += Math.sin(i * 0.1) * Math.cos(i * 0.1)
                    * Math.tan(i * 0.01) * Math.log(i + 1);

            // 添加内存分配加重GC压力
            if (i % 500 == 0) {
                String temp = new String("heavy_calc_" + i + "_" + result);
            }
        }
    }

    private void updatePhysics() {
        // 更新位置
        circleX += dx;
        circleY += dy;

        // 添加随机扰动增加计算复杂度
        circleX += (random.nextFloat() - 0.5f) * 8;
        circleY += (random.nextFloat() - 0.5f) * 8;

        // 更复杂的边界检测
        if (circleX - circleRadius <= 0) {
            dx = Math.abs(dx) * (0.7f + random.nextFloat() * 0.6f);
            circleX = circleRadius;
        } else if (circleX + circleRadius >= getWidth()) {
            dx = -Math.abs(dx) * (0.7f + random.nextFloat() * 0.6f);
            circleX = getWidth() - circleRadius;
        }

        if (circleY - circleRadius <= 0) {
            dy = Math.abs(dy) * (0.7f + random.nextFloat() * 0.6f);
            circleY = circleRadius;
        } else if (circleY + circleRadius >= getHeight()) {
            dy = -Math.abs(dy) * (0.7f + random.nextFloat() * 0.6f);
            circleY = getHeight() - circleRadius;
        }

        // 额外的计算负担
        calculateAdditionalPhysics();
    }

    /**
     * 额外的物理计算
     */
    private void calculateAdditionalPhysics() {
        // 制造更多的计算负担
        double dummy = 0;
        for (int i = 0; i < 2000; i++) {
            dummy += Math.sqrt(i) * Math.pow(Math.sin(i * 0.1), 2);
        }
    }

    /**
     * 停止所有动画
     */
    public void stopAnimation() {
        isAnimating = false;
        mOnDrawInvalidateMode = false;

        // 移除所有Handler的回调
        handler1.removeCallbacksAndMessages(null);
        handler2.removeCallbacksAndMessages(null);
        handler3.removeCallbacksAndMessages(null);

        // 清理资源
        postInvalidate();
    }

    public void setUpdateListener(OnAnimationUpdateListener listener) {
        this.updateListener = listener;
    }

    public interface OnAnimationUpdateListener {
        void onInvalidateCalled(int count, int fps);
        void onFPSUpdated(int fps);
    }

    public int getInvalidateCount() {
        return invalidateCount;
    }

    public int getCurrentFPS() {
        return currentFPS;
    }

    /**
     * 获取当前动画状态
     */
    public boolean isAnimating() {
        return isAnimating;
    }
}