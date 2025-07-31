package com.evenbus.view.circle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CircleChargeAnimationViewReally extends View {
    // 圆环参数
    private Paint mRingPaint;
    private RectF mRingRect;
    private int[] mRingColors = {
            Color.parseColor("#FF0000"), // 红 (0°)
            Color.parseColor("#00FF00"), // 绿 (90°)
            Color.parseColor("#0000FF"), // 蓝 (180°)
            Color.parseColor("#FF0000")  // 红 (270°)
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
    private List<Particle> mPendingParticles = new ArrayList<>();
    private Paint mParticlePaint; // 重用Paint对象

    private Random mRandom = new Random();
    private long mLastParticleTime = 0;
    private static final long PARTICLE_INTERVAL = 200;
    private static final int MAX_PARTICLES = 100;

    // 中心点
    private float mCenterX, mCenterY;
    private float mSafeZoneRadius;

    // 水滴吸附效果参数
    private float DROP_ATTRACTION_RANGE = 1.2f; // 圆环外1.2倍半径范围
    private float DROP_ABSORB_DISTANCE = 0.7f; // 吸附融合距离
    private float DROP_ABSORB_SPEED_FACTOR = 2.5f; // 吸附加速因子

    // 涟漪效果
    private List<Ripple> mRipples = new ArrayList<>();
    private Paint mRipplePaint; // 重用Paint对象

    public CircleChargeAnimationViewReally(Context context) {
        super(context);
        init();
    }

    public CircleChargeAnimationViewReally(Context context, AttributeSet attrs) {
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

        // 初始化粒子画笔（重用）
        mParticlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mParticlePaint.setStyle(Paint.Style.FILL);

        // 初始化涟漪画笔（重用）
        mRipplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRipplePaint.setStyle(Paint.Style.STROKE);
        mRipplePaint.setStrokeWidth(4);

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

        // 创建圆环渐变着色器
        SweepGradient sweepGradient = new SweepGradient(
                mCenterX, mCenterY,
                mRingColors,
                new float[]{0f, 0.25f, 0.5f, 0.75f}
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

        // 绘制涟漪效果
        drawRipples(canvas);

        // 绘制文本
        float textY = mCenterY - (mTextPaint.descent() + mTextPaint.ascent()) / 2;
        canvas.drawText(mPercentText, mCenterX, textY - mTextSpacing, mTextPaint);
        canvas.drawText(mChargeText, mCenterX, textY + mTextSize + mTextSpacing, mTextPaint);

        // 持续动画
        invalidate();
    }

    private void drawRipples(Canvas canvas) {
        // 使用重用涟漪画笔
        Iterator<Ripple> iterator = mRipples.iterator();
        while (iterator.hasNext()) {
            Ripple ripple = iterator.next();

            // 更新涟漪
            ripple.update();

            // 设置颜色和透明度
            mRipplePaint.setColor(ripple.color);
            mRipplePaint.setAlpha((int)(255 * (1 - ripple.progress)));

            // 绘制涟漪
            canvas.drawCircle(ripple.centerX, ripple.centerY,
                    ripple.size * ripple.progress, mRipplePaint);

            // 检查是否完成
            if (ripple.progress >= 1.0f) {
                iterator.remove();
            }
        }
    }

    private void generateParticles() {
        // 检查粒子总数是否超过限制
        int totalParticles = mParticles.size() + mPendingParticles.size();
        if (totalParticles >= MAX_PARTICLES) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastParticleTime > PARTICLE_INTERVAL) {
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
                        mCenterX, mCenterY, mRingRadius, mRingWidth, mInnerCircleRadius
                ));
            }
        }
    }

    private int getRingColorAtAngle(float angle) {
        // 直接使用传入的角度（0°指向顶部）
        float normalizedPos = angle / 360f;

        // 根据角度位置返回精确颜色
        if (normalizedPos < 0.25f) {
            float fraction = normalizedPos / 0.25f;
            return interpolateColor(mRingColors[0], mRingColors[1], fraction);
        } else if (normalizedPos < 0.5f) {
            float fraction = (normalizedPos - 0.25f) / 0.25f;
            return interpolateColor(mRingColors[1], mRingColors[2], fraction);
        } else if (normalizedPos < 0.75f) {
            float fraction = (normalizedPos - 0.5f) / 0.25f;
            return interpolateColor(mRingColors[2], mRingColors[3], fraction);
        } else {
            return mRingColors[3];
        }
    }

    private int interpolateColor(int color1, int color2, float fraction) {
        fraction = Math.max(0, Math.min(1, fraction));

        int a1 = (color1 >> 24) & 0xff;
        int r1 = (color1 >> 16) & 0xff;
        int g1 = (color1 >> 8) & 0xff;
        int b1 = color1 & 0xff;

        int a2 = (color2 >> 24) & 0xff;
        int r2 = (color2 >> 16) & 0xff;
        int g2 = (color2 >> 8) & 0xff;
        int b2 = color2 & 0xff;

        int a = (int)(a1 + (a2 - a1) * fraction);
        int r = (int)(r1 + (r2 - r1) * fraction);
        int g = (int)(g1 + (g2 - g1) * fraction);
        int b = (int)(b1 + (b2 - b1) * fraction);

        return Color.argb(a, r, g, b);
    }

    private void drawAndUpdateParticles(Canvas canvas) {
        // 使用迭代器安全删除
        Iterator<Particle> iterator = mParticles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();

            // 更新粒子位置和大小
            particle.update(mPendingParticles);

            // 设置粒子颜色和透明度
            mParticlePaint.setColor(particle.getCurrentColor());
            mParticlePaint.setAlpha(particle.alpha);

            // 绘制粒子
            if (particle.isStretched) {
                // 保存当前画布状态
                canvas.save();

                // 应用拉伸变换
                canvas.translate(particle.x, particle.y);
                canvas.scale(particle.stretchX, particle.stretchY);
                canvas.translate(-particle.x, -particle.y);

                // 绘制拉伸后的粒子
                canvas.drawCircle(particle.x, particle.y, particle.size, mParticlePaint);

                // 恢复画布状态
                canvas.restore();
            } else {
                // 正常绘制圆形粒子
                canvas.drawCircle(particle.x, particle.y, particle.size, mParticlePaint);
            }

            // 检查是否应该移除
            if (particle.isCollided()) {
                iterator.remove();
            }
        }

        // 在遍历结束后添加新粒子
        if (!mPendingParticles.isEmpty()) {
            // 调试日志：显示添加的粒子数量
            Log.d("ParticleSystem", "Adding " + mPendingParticles.size() + " new particles");
            mParticles.addAll(mPendingParticles);
            mPendingParticles.clear();
        }

        // 调试日志：显示当前粒子总数
        if (mParticles.size() > 50) {
            Log.d("ParticleSystem", "Total particles: " + mParticles.size());
        }
    }

    // 设置百分比
    public void setPercentage(int percentage) {
        mPercentText = percentage + "%";
        invalidate();
    }

    // 设置充电文本
    public void setChargeText(String text) {
        mChargeText = text;
        invalidate();
    }

    // 水滴吸附效果参数设置
    public void setDropAttractionRange(float range) {
        DROP_ATTRACTION_RANGE = range;
    }

    public void setDropAbsorbSpeedFactor(float factor) {
        DROP_ABSORB_SPEED_FACTOR = factor;
    }

    // 粒子类（包含水滴吸附效果）
    private class Particle {
        // 位置属性
        float x, y;
        float size;

        // 颜色属性
        int startColor;
        int targetColor;
        int alpha = 255;

        // 运动参数
        float startX, startY;
        float startSize;
        float targetX, targetY;
        float targetSize;
        float progress = 0f;
        float speed;

        // 圆环参数（用于吸附效果）
        private float mRingRadius;
        private float mRingWidth;
        private float mInnerCircleRadius;

        // 吸附效果状态
        private boolean mIsAttracted = false;
        private float mAttractionProgress = 0f;
        private float mAttractionStartX, mAttractionStartY;
        private float mAttractionTargetX, mAttractionTargetY;
        private float mAttractionStartSize;
        private float mOriginalSpeed;

        // 水滴变形参数
        boolean isStretched = false;
        float stretchX = 1.0f;
        float stretchY = 1.0f;

        // 碰撞检测
        private float mCenterX, mCenterY;
        private boolean mCollided = false;

        // 位置差值（用于计算）
        private float mDx, mDy;

        // 粒子生命周期管理
        private float mLifeTime = 0f;
        private static final float MAX_LIFE_TIME = 5.0f; // 粒子最多存在5秒

        Particle(float startX, float startY, float startSize, int startColor,
                 float targetX, float targetY, float targetSize, float speed,
                 int targetColor, float centerX, float centerY,
                 float ringRadius, float ringWidth, float innerCircleRadius) {
            // 初始化位置
            this.startX = this.x = startX;
            this.startY = this.y = startY;
            this.startSize = this.size = startSize;

            // 初始化颜色
            this.startColor = startColor;
            this.targetColor = targetColor;

            // 初始化目标
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetSize = targetSize;
            this.speed = speed;

            // 初始化环境
            this.mCenterX = centerX;
            this.mCenterY = centerY;
            this.mRingRadius = ringRadius;
            this.mRingWidth = ringWidth;
            this.mInnerCircleRadius = innerCircleRadius;
            this.mOriginalSpeed = speed;
        }

        void update(List<Particle> newParticles) {
            // 更新生命周期
            mLifeTime += 0.016f; // 假设每帧约16ms

            // 检查生命周期结束
            if (mLifeTime > MAX_LIFE_TIME) {
                mCollided = true;
                return;
            }

            // 计算当前位置到中心的距离
            mDx = x - mCenterX;
            mDy = y - mCenterY;
            float distance = (float) Math.sqrt(mDx * mDx + mDy * mDy);

            // 计算圆环外边缘位置
            float ringOuterRadius = mRingRadius + mRingWidth / 2;

            // 检查是否进入水滴吸附范围
            if (!mIsAttracted && distance < ringOuterRadius * DROP_ATTRACTION_RANGE) {
                startAttractionAnimation();
            }

            if (mIsAttracted) {
                updateAttractionAnimation(newParticles);
            } else {
                // 常规移动
                progress = Math.min(progress + speed, 1.0f);
                x = startX + (targetX - startX) * progress;
                y = startY + (targetY - startY) * progress;
                size = startSize + (targetSize - startSize) * progress;
            }

            // 碰撞检测（在内圆处消失）
            checkCollision();
        }

        private void checkCollision() {
            if (mCollided) return;

            // 使用成员变量mDx和mDy计算距离
            float distance = (float) Math.sqrt(mDx * mDx + mDy * mDy);

            // 碰撞条件：粒子中心到视图中心的距离小于（内圆半径 + 粒子半径）
            if (distance < mInnerCircleRadius + size) {
                mCollided = true;
            }
        }

        private void startAttractionAnimation() {
            mIsAttracted = true;
            mAttractionProgress = 0f;
            mAttractionStartX = x;
            mAttractionStartY = y;
            mAttractionStartSize = size;

            // 使用成员变量mDx和mDy计算角度
            float angle = (float) Math.atan2(mDy, mDx);

            // 计算圆环上的吸附目标点（圆环外边缘）
            mAttractionTargetX = mCenterX + (float) Math.cos(angle) *
                    (mRingRadius + mRingWidth / 2);
            mAttractionTargetY = mCenterY + (float) Math.sin(angle) *
                    (mRingRadius + mRingWidth / 2);

            // 吸附时加速
            speed = mOriginalSpeed * DROP_ABSORB_SPEED_FACTOR;
        }

        private void updateAttractionAnimation(List<Particle> newParticles) {
            // 更新吸附进度
            mAttractionProgress = Math.min(mAttractionProgress + speed * 3, 1.0f);

            // 使用缓动函数（三次缓动）
            float easedProgress = (float) Math.pow(mAttractionProgress, 0.7);

            // 位置更新（向圆环外边缘移动）
            x = mAttractionStartX + (mAttractionTargetX - mAttractionStartX) * easedProgress;
            y = mAttractionStartY + (mAttractionTargetY - mAttractionStartY) * easedProgress;

            // 重置变形
            isStretched = false;
            stretchX = 1.0f;
            stretchY = 1.0f;

            // 大小变化（接近时变形）
            float sizeFactor;
            if (mAttractionProgress < 0.7f) {
                // 初期轻微膨胀
                sizeFactor = 1 + (0.3f * (mAttractionProgress / 0.7f));
            } else {
                // 后期拉伸变形
                float stretchFactor = (mAttractionProgress - 0.7f) / 0.3f;
                sizeFactor = 1.3f - (0.3f * stretchFactor);

                // X方向拉伸，Y方向压缩（模拟水滴接触时的变形）
                stretchX = 1.0f + 0.5f * stretchFactor;
                stretchY = 1.0f - 0.3f * stretchFactor;
                isStretched = true;
            }

            size = mAttractionStartSize * sizeFactor;

            // 接近吸附点时触发融合
            if (mAttractionProgress > DROP_ABSORB_DISTANCE) {
                // 创建融合效果
                createAbsorbEffect(newParticles);

                // 标记粒子为完成吸附
                mCollided = true;
            }
        }

        private void createAbsorbEffect(List<Particle> newParticles) {
            // 1. 创建涟漪效果
            int rippleColor = getCurrentColor();
            float rippleSize = size * 3.0f;

            mRipples.add(new Ripple(x, y, rippleSize, rippleColor));

            // 2. 创建小水滴飞溅效果
            createSplashParticles(newParticles);
        }

        private void createSplashParticles(List<Particle> newParticles) {
            // 检查是否还能添加新粒子
            int totalParticles = mParticles.size() + newParticles.size();
            if (totalParticles >= MAX_PARTICLES - 5) {
                return; // 避免超过最大限制
            }

            // 创建3-5个小水滴粒子
            int splashCount = 3 + mRandom.nextInt(3);
            for (int i = 0; i < splashCount; i++) {
                // 随机角度
                float angle = mRandom.nextFloat() * 360;
                float rad = (float) Math.toRadians(angle);

                // 起始位置在当前粒子位置
                float splashStartX = x;
                float splashStartY = y;

                // 目标位置（随机偏移）
                float splashDistance = size * (2 + mRandom.nextFloat() * 3);
                float splashTargetX = x + (float) Math.cos(rad) * splashDistance;
                float splashTargetY = y + (float) Math.sin(rad) * splashDistance;

                // 大小和速度
                float splashSize = size * (0.3f + mRandom.nextFloat() * 0.3f);
                float splashSpeed = speed * (0.5f + mRandom.nextFloat());

                // 创建粒子 - 使用极低透明度而不是完全透明
                Particle splash = new Particle(
                        splashStartX, splashStartY, splashSize, getCurrentColor(),
                        splashTargetX, splashTargetY, splashSize * 0.1f, splashSpeed,
                        Color.argb(10, 0, 0, 0), // 极低透明度
                        mCenterX, mCenterY, mRingRadius, mRingWidth, mInnerCircleRadius
                );

                // 飞溅粒子直接进入消失状态
                splash.mCollided = false;
                splash.alpha = 200; // 半透明

                // 添加到临时列表
                newParticles.add(splash);
            }
        }

        // 获取当前颜色（不包含透明度）
        int getCurrentColor() {
            return interpolateColor(startColor, targetColor, progress);
        }

        // 检查粒子是否应该移除
        boolean isCollided() {
            return mCollided;
        }
    }

    // 涟漪效果类
    private class Ripple {
        float centerX, centerY;
        float size;
        int color;
        float progress = 0f;
        final float speed = 0.05f;

        Ripple(float x, float y, float size, int color) {
            this.centerX = x;
            this.centerY = y;
            this.size = size;
            this.color = color;
        }

        void update() {
            progress = Math.min(progress + speed, 1.0f);
        }
    }
}