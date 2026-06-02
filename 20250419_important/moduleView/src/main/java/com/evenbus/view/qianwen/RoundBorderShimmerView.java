package com.evenbus.view.qianwen;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class RoundBorderShimmerView extends View {
    private final float mCardRadius = 28f;
    private final float mStrokeWidth = 6f;

    private Path mBorderPath = new Path();
    private PathMeasure mPathMeasure = new PathMeasure();
    private float mTotalLength;
    private float mShimmerLength;
    private float mAnimOffset = 0f;
    private ValueAnimator mAnimator;

    private Paint mBgPaint;
    private Paint mShimmerPaint;
    private Paint mGlowPaint;

    private SweepGradient mSweepGradient;  // 扫描渐变
    private Matrix mGradientMatrix;        // 用于旋转渐变

    public RoundBorderShimmerView(Context context) {
        super(context);
        init();
    }

    public RoundBorderShimmerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(0xFF121212);

        mShimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShimmerPaint.setStyle(Paint.Style.STROKE);
        mShimmerPaint.setStrokeWidth(mStrokeWidth);
        mShimmerPaint.setStrokeCap(Paint.Cap.ROUND);

        mGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGlowPaint.setStyle(Paint.Style.STROKE);
        mGlowPaint.setStrokeWidth(mStrokeWidth + 10f);
        mGlowPaint.setStrokeCap(Paint.Cap.ROUND);

        mGradientMatrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        RectF rectF = new RectF(
                mStrokeWidth / 2,
                mStrokeWidth / 2,
                w - mStrokeWidth / 2,
                h - mStrokeWidth / 2
        );
        mBorderPath.reset();
        mBorderPath.addRoundRect(rectF, mCardRadius, mCardRadius, Path.Direction.CW);

        mPathMeasure.setPath(mBorderPath, true);
        mTotalLength = mPathMeasure.getLength();
        mShimmerLength = mTotalLength / 4f;

        // 创建扫描渐变（基于卡片中心）
        createSweepGradient(w, h);

        startAnimation();
    }

    /**
     * 创建扫描渐变
     * 渐变围绕中心旋转一周，完美贴合圆角边框
     */
    private void createSweepGradient(int width, int height) {
        float centerX = width / 2f;
        float centerY = height / 2f;

        // 扫描渐变的颜色数组（0度对应右边，顺时针旋转）
        // 为了让光晕效果更好，设置多段渐变
        int[] colors = {
                Color.TRANSPARENT,   // 0°
                0xFFA855F7,          // 45° 紫色
                0xFFC084FC,          // 90° 浅紫
                0xFFE9D5FF,          // 135° 更浅紫
                0xFFFFFFFF,          // 180° 亮白（正下方）
                0xFFE9D5FF,          // 225°
                0xFFC084FC,          // 270°
                0xFFA855F7,          // 315°
                Color.TRANSPARENT    // 360°
        };

        // 位置数组（0-1之间）
        float[] positions = {
                0f,     // 透明
                0.125f, // 45° 紫色
                0.25f,  // 90° 浅紫
                0.375f, // 135° 更浅紫
                0.5f,   // 180° 亮白中心
                0.625f, // 225°
                0.75f,  // 270°
                0.875f, // 315°
                1f      // 360° 透明
        };

        mSweepGradient = new SweepGradient(centerX, centerY, colors, positions);

        // 可选：旋转渐变，让亮白区域从底部开始
        // mGradientMatrix.setRotate(180, centerX, centerY);
        // mSweepGradient.setLocalMatrix(mGradientMatrix);
    }

    private void startAnimation() {
        mAnimator = ValueAnimator.ofFloat(0, mTotalLength);
        mAnimator.setDuration(4000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mAnimOffset = (float) animation.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. 画黑色卡片背景
        RectF cardRect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(cardRect, mCardRadius, mCardRadius, mBgPaint);

        // 2. 获取当前要显示的流光路径段（顺时针从底部开始）
        float start = mAnimOffset;
        float end = start + mShimmerLength;
        Path segment = new Path();

        if (end <= mTotalLength) {
            mPathMeasure.getSegment(start, end, segment, true);
        } else {
            mPathMeasure.getSegment(start, mTotalLength, segment, true);
            Path secondSegment = new Path();
            mPathMeasure.getSegment(0, end - mTotalLength, secondSegment, true);
            segment.addPath(secondSegment);
        }
        segment.rLineTo(0, 0);

        // 3. 绘制光晕层（模糊 + 扫描渐变）
        canvas.saveLayer(0, 0, getWidth(), getHeight(), null);

        mGlowPaint.setShader(mSweepGradient);
        mGlowPaint.setMaskFilter(new BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL));
        mGlowPaint.setAlpha(80);  // 半透明光晕
        canvas.drawPath(segment, mGlowPaint);

        // 4. 绘制主流光层（清晰 + 扫描渐变）
        mShimmerPaint.setShader(mSweepGradient);
        mShimmerPaint.setMaskFilter(null);
        mShimmerPaint.setAlpha(255);
        canvas.drawPath(segment, mShimmerPaint);

        canvas.restore();

        // 清理引用
        mShimmerPaint.setShader(null);
        mGlowPaint.setShader(null);
        mGlowPaint.setMaskFilter(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }
}