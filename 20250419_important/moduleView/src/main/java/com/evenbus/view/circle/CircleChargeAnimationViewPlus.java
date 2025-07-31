package com.evenbus.view.circle;

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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CircleChargeAnimationViewPlus extends View {
    // 圆环参数
    private Paint mRingPaint;
    private RectF mRingRect;
    private int[] mRingColors = {
            Color.parseColor("#FF0000"), // 红
            Color.parseColor("#00FF00"), // 绿
            Color.parseColor("#0000FF"), // 蓝
            Color.parseColor("#FF0000")  // 红（闭合）
    };
    private float mRingWidth = 60f;
    private float mRingRadius;
    private float mInnerCircleRadius;

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
    private static final long PARTICLE_INTERVAL = 200;
    private static final int MAX_PARTICLES = 100;

    // 中心点
    private float mCenterX, mCenterY;
    private float mSafeZoneRadius;

    // 调试开关
    private static final boolean DEBUG = true;

    public CircleChargeAnimationViewPlus(Context context) {
        super(context);
        init();
    }

    public CircleChargeAnimationViewPlus(Context context, AttributeSet attrs) {
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
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        setBackgroundColor(Color.BLACK);
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
        mInnerCircleRadius = mRingRadius - mRingWidth/2;
        mSafeZoneRadius = minDimension * 0.25f;

        // 创建圆环矩形区域
        mRingRect = new RectF(
                mCenterX - mRingRadius,
                mCenterY - mRingRadius,
                mCenterX + mRingRadius,
                mCenterY + mRingRadius
        );

        // 创建圆环渐变着色器（使用四个颜色点确保精确分布）
        SweepGradient sweepGradient = new SweepGradient(
                mCenterX, mCenterY,
                mRingColors,
                new float[]{0f, 0.25f, 0.5f, 0.75f} // 红(0°), 绿(90°), 蓝(180°), 红(270°)
        );

        // 旋转渐变使0°指向顶部
        Matrix gradientMatrix = new Matrix();
        gradientMatrix.setRotate(-90, mCenterX, mCenterY);
        sweepGradient.setLocalMatrix(gradientMatrix);

        mRingPaint.setShader(sweepGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 生成新粒子
        generateParticles();

        // 绘制圆环
        canvas.drawArc(mRingRect, 0, 360, false, mRingPaint);

        // 绘制黄色内圆
//        canvas.drawCircle(mCenterX, mCenterY, mInnerCircleRadius, mInnerCirclePaint);

        // 绘制粒子并更新
        drawAndUpdateParticles(canvas);

        // 绘制文本
        float textY = mCenterY - (mTextPaint.descent() + mTextPaint.ascent()) / 2;
        canvas.drawText(mPercentText, mCenterX, textY - mTextSpacing, mTextPaint);
        canvas.drawText(mChargeText, mCenterX, textY + mTextSize + mTextSpacing, mTextPaint);

        // 调试点：在关键位置绘制标记
        if (DEBUG) {
            Paint debugPaint = new Paint();
            debugPaint.setStyle(Paint.Style.FILL);
            debugPaint.setStrokeWidth(10);

            // 关键点调试
            drawDebugPoint(canvas, debugPaint, 0);    // 顶部
            drawDebugPoint(canvas, debugPaint, 90);   // 右侧
            drawDebugPoint(canvas, debugPaint, 180);  // 底部
            drawDebugPoint(canvas, debugPaint, 270);  // 左侧
        }

        // 持续动画
        invalidate();
    }

    private void drawDebugPoint(Canvas canvas, Paint paint, float angle) {
        // 修正角度：0°指向顶部（Y轴负方向）
        float rad = (float) Math.toRadians(angle - 90);
        float x = mCenterX + (float) Math.cos(rad) * mRingRadius-30;
        float y = mCenterY + (float) Math.sin(rad) * mRingRadius;
        int color = getRingColorAtAngle(angle);

        paint.setColor(color);
        canvas.drawCircle(x, y, 10, paint);

        // 添加文本标签
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(20);
        canvas.drawText(String.format("%.0f°", angle), x + 15, y, textPaint);
    }

    private void generateParticles() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastParticleTime > PARTICLE_INTERVAL && mParticles.size() < MAX_PARTICLES) {
            mLastParticleTime = currentTime;

            // 随机角度 (0-360度)
            float angle = mRandom.nextFloat() * 360;
            float normalizedAngle = (angle + 360) % 360;

            // 避开顶部区域（-30度到30度之间不生成粒子）
            if (normalizedAngle > 30 && normalizedAngle < 330) {
                // 粒子起始位置在圆环外1.5-2倍半径处
                float distanceFactor = 1.5f + mRandom.nextFloat() * 0.5f;

                // 修正角度：0°指向顶部（Y轴负方向）
                float rad = (float) Math.toRadians(angle - 90);
                float startX = mCenterX + (float) Math.cos(rad) * mRingRadius * distanceFactor;
                float startY = mCenterY + (float) Math.sin(rad) * mRingRadius * distanceFactor;

                // 目标位置在黄色内圆边缘（确保在安全区域外）
                float targetDistance = Math.max(mInnerCircleRadius, mSafeZoneRadius);
                float targetX = mCenterX + (float) Math.cos(rad) * targetDistance;
                float targetY = mCenterY + (float) Math.sin(rad) * targetDistance;

                // 随机初始大小
                float startSize = mRandom.nextFloat() * (mRingWidth / 4) + 2;

                // 目标大小
                float targetSize = mRingWidth / 2;

                // 随机移动速度
                float speed = 0.01f + mRandom.nextFloat() * 0.04f;

                // 计算目标位置在圆环上的颜色
                int targetColor = getRingColorAtAngle(angle);

                // 起始颜色（半透明）
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
     * 修正颜色计算方法（确保精确匹配预期位置）
     */
    private int getRingColorAtAngle(float angle) {
        // 直接使用传入的角度（0°指向顶部）
        float normalizedPos = angle / 360f;

        // 根据角度位置返回精确颜色
        if (normalizedPos < 0.25f) { // 0°-90°：红到绿渐变
            float fraction = normalizedPos / 0.25f;
            return interpolateColor(mRingColors[0], mRingColors[1], fraction);
        } else if (normalizedPos < 0.5f) { // 90°-180°：绿到蓝渐变
            float fraction = (normalizedPos - 0.25f) / 0.25f;
            return interpolateColor(mRingColors[1], mRingColors[2], fraction);
        } else if (normalizedPos < 0.75f) { // 180°-270°：蓝到红渐变
            float fraction = (normalizedPos - 0.5f) / 0.25f;
            return interpolateColor(mRingColors[2], mRingColors[3], fraction);
        } else { // 270°-360°：红色
            return mRingColors[3];
        }
    }

    /**
     * 优化的颜色插值方法
     */
    private int interpolateColor(int color1, int color2, float fraction) {
        // 确保fraction在0-1范围内
        fraction = Math.max(0, Math.min(1, fraction));

        int a1 = (color1 >> 24) & 0xff;
        int r1 = (color1 >> 16) & 0xff;
        int g1 = (color1 >> 8) & 0xff;
        int b1 = color1 & 0xff;

        int a2 = (color2 >> 24) & 0xff;
        int r2 = (color2 >> 16) & 0xff;
        int g2 = (color2 >> 8) & 0xff;
        int b2 = color2 & 0xff;

        // 使用线性插值
        int a = (int)(a1 + (a2 - a1) * fraction);
        int r = (int)(r1 + (r2 - r1) * fraction);
        int g = (int)(g1 + (g2 - g1) * fraction);
        int b = (int)(b1 + (b2 - b1) * fraction);

        return Color.argb(a, r, g, b);
    }

    private void drawAndUpdateParticles(Canvas canvas) {
        Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setStyle(Paint.Style.FILL);

        // 使用迭代器安全删除
        Iterator<Particle> iterator = mParticles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();

            // 更新粒子位置和大小
            particle.update();

            // 设置粒子颜色
            particlePaint.setColor(particle.getCurrentColor());

            // 绘制粒子
            canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint);

            // 检查是否到达目标或发生碰撞
            if (particle.isAtTarget() || particle.isCollided()) {
                iterator.remove();
            }
        }
    }

    // 设置百分比
    public void setPercentage(int percentage) {
        mPercentText = percentage + "%";
        invalidate();
    }

    public void setChargeText(String text) {

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
        private float mCenterX, mCenterY;
        private float mInnerCircleRadius;
        private boolean mCollided = false;

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

        private void checkCollision() {
            if (mCollided) return;

            float dx = x - mCenterX;
            float dy = y - mCenterY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < mInnerCircleRadius + size) {
                mCollided = true;
            }
        }

        int getCurrentColor() {
            return interpolateColor(startColor, targetColor, progress);
        }

        boolean isAtTarget() {
            return progress >= 1.0f;
        }

        boolean isCollided() {
            return mCollided;
        }
    }
}
