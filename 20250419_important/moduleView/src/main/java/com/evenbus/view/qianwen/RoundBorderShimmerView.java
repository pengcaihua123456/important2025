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

    private SweepGradient mSweepGradient;
    private Matrix mGradientMatrix;
    private float centerX, centerY;

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
        mShimmerPaint.setStrokeJoin(Paint.Join.ROUND);

        mGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGlowPaint.setStyle(Paint.Style.STROKE);
        mGlowPaint.setStrokeWidth(mStrokeWidth + 8f);
        mGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        mGlowPaint.setStrokeJoin(Paint.Join.ROUND);

        mGradientMatrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w / 2f;
        centerY = h / 2f;

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

        createSweepGradient();

        startAnimation();
    }

    private void createSweepGradient() {
        // 注意：这里先不旋转，让渐变从0°开始（右边）
        int[] colors = {
                Color.TRANSPARENT,
                0x40A855F7,  // 半透明紫色，让过渡更自然
                0x80C084FC,
                0xCCE9D5FF,
                0xFFFFFFFF,  // 亮白
                0xCCE9D5FF,
                0x80C084FC,
                0x40A855F7,
                Color.TRANSPARENT
        };

        float[] positions = {
                0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f
        };

        mSweepGradient = new SweepGradient(centerX, centerY, colors, positions);
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

        // 获取流光路径段
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

        // ========== 关键修复：让渐变跟随流光旋转 ==========
        // 计算流光中心点在整个路径上的进度
        float progress = (mAnimOffset + mShimmerLength / 2) / mTotalLength;
        // 转换为角度（顺时针，0°在右边）
        float angle = progress * 360f;
        // 旋转渐变，让亮白区域始终在流光中心
        mGradientMatrix.reset();
        mGradientMatrix.setRotate(angle, centerX, centerY);
        mSweepGradient.setLocalMatrix(mGradientMatrix);

        // 绘制多层光晕
        canvas.saveLayer(0, 0, getWidth(), getHeight(), null);

        // 外层光晕（宽+模糊）
        mGlowPaint.setShader(mSweepGradient);
        mGlowPaint.setMaskFilter(new BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL));
        mGlowPaint.setAlpha(60);
        mGlowPaint.setStrokeWidth(mStrokeWidth + 12f);
        canvas.drawPath(segment, mGlowPaint);

        // 中层光晕
        mGlowPaint.setMaskFilter(new BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL));
        mGlowPaint.setAlpha(100);
        mGlowPaint.setStrokeWidth(mStrokeWidth + 6f);
        canvas.drawPath(segment, mGlowPaint);

        // 主流光
        mShimmerPaint.setShader(mSweepGradient);
        mShimmerPaint.setMaskFilter(null);
        mShimmerPaint.setAlpha(255);
        canvas.drawPath(segment, mShimmerPaint);

        canvas.restore();

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