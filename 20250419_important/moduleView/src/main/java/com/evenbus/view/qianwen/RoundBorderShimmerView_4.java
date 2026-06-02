package com.evenbus.view.qianwen;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class RoundBorderShimmerView_4 extends View {
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

    public RoundBorderShimmerView_4(Context context) {
        super(context);
        init();
    }

    public RoundBorderShimmerView_4(Context context, AttributeSet attrs) {
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

        startAnimation();
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

        // 画背景
        RectF cardRect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(cardRect, mCardRadius, mCardRadius, mBgPaint);

        // 获取当前流光路径段
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

        // ========== 核心修复：使用 BlurMaskFilter 实现连续光晕 ==========
        // 不需要分段，用带透明度的纯色 + 模糊滤镜，自然产生渐变感

        // 保存原 paint 设置
        int originalColor = mShimmerPaint.getColor();
        MaskFilter originalMask = mShimmerPaint.getMaskFilter();

        // 方案1：纯色光带 + 边缘模糊（产生自然的淡入淡出效果）
        mShimmerPaint.setShader(null);
        mShimmerPaint.setColor(0xCCE0B0FF);  // 半透明紫色
        mShimmerPaint.setMaskFilter(new BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL));
        canvas.drawPath(segment, mShimmerPaint);

        // 方案2：再加一层更亮的中心光带（增加层次感）
        mShimmerPaint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
        mShimmerPaint.setColor(0xFFFFFFFF);  // 白色中心
        canvas.drawPath(segment, mShimmerPaint);

        // 恢复设置
        mShimmerPaint.setMaskFilter(originalMask);
        mShimmerPaint.setColor(originalColor);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) mAnimator.cancel();
    }
}