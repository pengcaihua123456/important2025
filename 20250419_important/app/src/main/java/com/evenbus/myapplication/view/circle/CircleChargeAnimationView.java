package com.evenbus.myapplication.view.circle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CircleChargeAnimationView extends View {
    // 圆环参数
    private Paint mRingPaint;
    private RectF mRingRect;
    private int[] mRingColors = {Color.RED, Color.GREEN, Color.BLUE};
    private float mRingWidth = 60f; // 圆环的宽度
    private float mRingRadius;
    private float mInnerCircleRadius; // 黄色内圆半径

    // 文本参数
    private Paint mTextPaint;
    private String mPercentText = "85%";
    private String mChargeText = "正在充电";
    private float mTextSize = 48f;
    private float mTextSpacing = 20f;

    // 黄色内圆
    private Paint mInnerCirclePaint;

    // 粒子系统
    private List<Particle> mParticles = new ArrayList<>();
    private Random mRandom = new Random();
    private long mLastParticleTime = 0;
    private static final long PARTICLE_INTERVAL = 200; // 每200ms生成一个粒子
    private static final int MAX_PARTICLES = 100; // 最大粒子数量限制

    // 中心点
    private float mCenterX, mCenterY;

    // 安全区域（避免粒子干扰文字）
    private float mSafeZoneRadius;

    // 颜色插值器
    private ArgbEvaluator mColorEvaluator = new ArgbEvaluator();

    public CircleChargeAnimationView(Context context) {
        super(context);
        init();
    }

    public CircleChargeAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 初始化圆环画笔
        mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(mRingWidth);
        mRingPaint.setStrokeCap(Paint.Cap.ROUND);

        // 初始化黄色内圆画笔
        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setColor(Color.YELLOW);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);

        // 初始化文本画笔
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(Color.WHITE); // 改为黑色，以便在黄色背景上可见
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 计算中心点
        mCenterX = w / 2f;
        mCenterY = h / 2f;

        // 计算圆环半径
        float minDimension = Math.min(w, h);
        mRingRadius = minDimension * 0.4f - mRingWidth/2;

        // 计算黄色内圆半径（圆环内半径）
        mInnerCircleRadius = mRingRadius - mRingWidth/2;

        // 计算安全区域半径（避免粒子干扰文字）
        mSafeZoneRadius = minDimension * 0.25f;

        // 创建圆环矩形区域
        mRingRect = new RectF(
                mCenterX - mRingRadius,
                mCenterY - mRingRadius,
                mCenterX + mRingRadius,
                mCenterY + mRingRadius
        );

        // 创建圆环渐变着色器
        SweepGradient sweepGradient = new SweepGradient(
                mCenterX, mCenterY,
                mRingColors,
                new float[]{0f, 0.33f, 0.66f}
        );

        // 旋转渐变使其从顶部开始
        Matrix gradientMatrix = new Matrix();
        gradientMatrix.preRotate(-90, mCenterX, mCenterY);
        sweepGradient.setLocalMatrix(gradientMatrix);

        mRingPaint.setShader(sweepGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 生成新粒子（按时间间隔持续生成）
        generateParticles();

        // 绘制圆环
        canvas.drawArc(mRingRect, 0, 360, false, mRingPaint);

        // 绘制黄色内圆
//        canvas.drawCircle(mCenterX, mCenterY, mInnerCircleRadius, mInnerCirclePaint);

        // 绘制粒子并更新
        drawAndUpdateParticles(canvas);

        // 绘制文本
        canvas.drawText(mPercentText, mCenterX, mCenterY - mTextSpacing, mTextPaint);
        canvas.drawText(mChargeText, mCenterX, mCenterY + mTextSpacing + mTextSize, mTextPaint);

        // 持续动画
        invalidate();
    }

    private void generateParticles() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastParticleTime > PARTICLE_INTERVAL && mParticles.size() < MAX_PARTICLES) {
            mLastParticleTime = currentTime;

            // 随机角度 (0-360度)
            float angle = mRandom.nextFloat() * 360;

            // 避开顶部区域（-60度到60度之间不生成粒子）
            float normalizedAngle = (angle + 360) % 360;
            if (normalizedAngle > 60 && normalizedAngle < 300) {
                // 粒子起始位置在圆环外1.5-2倍半径处
                float distanceFactor = 1.5f + mRandom.nextFloat() * 0.5f;
                float startX = mCenterX + (float) Math.cos(Math.toRadians(angle)) * mRingRadius * distanceFactor;
                float startY = mCenterY + (float) Math.sin(Math.toRadians(angle)) * mRingRadius * distanceFactor;

                // 目标位置在黄色内圆边缘（确保在安全区域外）
                float targetDistance = Math.max(mInnerCircleRadius, mSafeZoneRadius);
                float targetX = mCenterX + (float) Math.cos(Math.toRadians(angle)) * targetDistance;
                float targetY = mCenterY + (float) Math.sin(Math.toRadians(angle)) * targetDistance;

                // 随机初始大小 (最大不超过圆环宽度的一半)
                float startSize = mRandom.nextFloat() * (mRingWidth / 4) + 2; // 更小的初始大小

                // 目标大小为圆环截面半径（即圆环宽度的一半）
                float targetSize = mRingWidth / 2; // 最终等于圆环截面半径

                // 随机移动速度 (0.01-0.05)
                float speed = 0.01f + mRandom.nextFloat() * 0.04f;

                // 计算目标位置在圆环上的颜色
                int targetColor = getRingColorAtAngle(angle);

                // 起始颜色为半透明版本的目标颜色
                int startColor = Color.argb(100,
                        Color.red(targetColor),
                        Color.green(targetColor),
                        Color.blue(targetColor));

                mParticles.add(new Particle(
                        startX, startY, startSize, startColor,
                        targetX, targetY, targetSize, speed, targetColor,
                        mCenterX, mCenterY, mInnerCircleRadius
                ));
            }
        }
    }

    /**
     * 根据角度获取圆环上对应位置的颜色
     */
    private int getRingColorAtAngle(float angle) {
        // 调整角度以适应渐变旋转
        float adjustedAngle = (angle + 90) % 360;

        // 计算归一化位置 (0.0 - 1.0)
        float normalizedPos = adjustedAngle / 360f;

        // 圆环颜色分段
        if (normalizedPos < 0.33f) {
            // 红色到绿色渐变
            float fraction = normalizedPos / 0.33f;
            return (Integer) mColorEvaluator.evaluate(fraction, mRingColors[0], mRingColors[1]);
        } else if (normalizedPos < 0.66f) {
            // 绿色到蓝色渐变
            float fraction = (normalizedPos - 0.33f) / 0.33f;
            return (Integer) mColorEvaluator.evaluate(fraction, mRingColors[1], mRingColors[2]);
        } else {
            // 蓝色到红色渐变
            float fraction = (normalizedPos - 0.66f) / 0.34f;
            return (Integer) mColorEvaluator.evaluate(fraction, mRingColors[2], mRingColors[0]);
        }
    }

    private void drawAndUpdateParticles(Canvas canvas) {
        Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setStyle(Paint.Style.FILL);

        List<Particle> toRemove = new ArrayList<>();

        for (Particle particle : mParticles) {
            // 更新粒子位置和大小
            particle.update();

            // 设置粒子颜色
            particlePaint.setColor(particle.getCurrentColor());

            // 绘制粒子
            canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint);

            // 检查是否到达目标或发生碰撞
            if (particle.isAtTarget() || particle.isCollided()) {
                toRemove.add(particle);
            }
        }

        // 移除到达目标或碰撞的粒子
        mParticles.removeAll(toRemove);
    }

    // 设置百分比
    public void setPercentage(int percentage) {
        mPercentText = percentage + "%";
        invalidate();
    }

    // 粒子类
    private class Particle {
        float x, y;
        float size;
        int startColor;
        int targetColor;

        float startX, startY;
        float startSize;
        float targetX, targetY;
        float targetSize;

        float progress = 0f;
        float speed;

        // 碰撞检测相关
        private float mCenterX, mCenterY; // 视图中心点
        private float mInnerCircleRadius; // 黄色内圆半径
        private boolean mCollided = false; // 碰撞标记

        Particle(float startX, float startY, float startSize, int startColor,
                 float targetX, float targetY, float targetSize, float speed,
                 int targetColor, float centerX, float centerY, float innerCircleRadius) {
            this.startX = this.x = startX;
            this.startY = this.y = startY;
            this.startSize = this.size = startSize;
            this.startColor = startColor;
            this.targetColor = targetColor;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetSize = targetSize;
            this.speed = speed;
            this.mCenterX = centerX;
            this.mCenterY = centerY;
            this.mInnerCircleRadius = innerCircleRadius;
        }

        void update() {
            progress = Math.min(progress + speed, 1.0f);

            // 线性插值移动
            x = startX + (targetX - startX) * progress;
            y = startY + (targetY - startY) * progress;

            // 线性插值大小变化
            size = startSize + (targetSize - startSize) * progress;

            // 碰撞检测
            checkCollision();
        }

        /**
         * 检测粒子是否与黄色内圆发生碰撞
         */
        private void checkCollision() {
            if (mCollided) return; // 已经碰撞过的不再检测

            // 计算粒子中心到视图中心的距离
            float dx = x - mCenterX;
            float dy = y - mCenterY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // 碰撞条件：粒子中心到圆心距离 < (内圆半径 + 粒子半径)
            if (distance < mInnerCircleRadius + size) {
                mCollided = true;
            }
        }

        /**
         * 获取当前颜色（根据进度插值）
         */
        int getCurrentColor() {
            return (Integer) mColorEvaluator.evaluate(progress, startColor, targetColor);
        }

        boolean isAtTarget() {
            return progress >= 1.0f;
        }

        boolean isCollided() {
            return mCollided;
        }
    }

    // 颜色插值器
    private static class ArgbEvaluator {
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            int startInt = (Integer) startValue;
            int startA = (startInt >> 24) & 0xff;
            int startR = (startInt >> 16) & 0xff;
            int startG = (startInt >> 8) & 0xff;
            int startB = startInt & 0xff;

            int endInt = (Integer) endValue;
            int endA = (endInt >> 24) & 0xff;
            int endR = (endInt >> 16) & 0xff;
            int endG = (endInt >> 8) & 0xff;
            int endB = endInt & 0xff;

            // 分别计算各分量
            int a = startA + (int)(fraction * (endA - startA));
            int r = startR + (int)(fraction * (endR - startR));
            int g = startG + (int)(fraction * (endG - startG));
            int b = startB + (int)(fraction * (endB - startB));

            // 组合为最终颜色值
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
}