package com.evenbus.view.qianwen;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class RoundBorderShimmerView_5 extends View {
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

    public RoundBorderShimmerView_5(Context context) {
        super(context);
        init();
    }

    public RoundBorderShimmerView_5(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 黑色背景
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(0xFF121212);

        // 主流光画笔（带渐变）
        mShimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShimmerPaint.setStyle(Paint.Style.STROKE);
        mShimmerPaint.setStrokeWidth(mStrokeWidth);
        mShimmerPaint.setStrokeCap(Paint.Cap.ROUND);

        // 光晕画笔（模糊效果）
        mGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGlowPaint.setStyle(Paint.Style.STROKE);
        mGlowPaint.setStrokeWidth(mStrokeWidth + 8f); // 比主光带宽，产生光晕
        mGlowPaint.setStrokeCap(Paint.Cap.ROUND);
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

        // 1. 画黑色卡片背景
        RectF cardRect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(cardRect, mCardRadius, mCardRadius, mBgPaint);

        // 2. 获取当前要显示的流光路径段
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

        // 3. 获取流光路径段的边界（用于创建渐变）
        RectF bounds = new RectF();
        segment.computeBounds(bounds, true);

        // 4. 创建沿路径方向的线性渐变
        // 关键：根据路径的主要方向来设置渐变方向
        float gradientStartX, gradientStartY, gradientEndX, gradientEndY;

        // 判断路径是更偏水平还是垂直
        float width = bounds.width();
        float height = bounds.height();

        if (width >= height) {
            // 水平方向为主：渐变从左到右
            gradientStartX = bounds.left;
            gradientStartY = bounds.centerY();
            gradientEndX = bounds.right;
            gradientEndY = bounds.centerY();
        } else {
            // 垂直方向为主：渐变从上到下
            gradientStartX = bounds.centerX();
            gradientStartY = bounds.top;
            gradientEndX = bounds.centerX();
            gradientEndY = bounds.bottom;
        }

        // 渐变色：两端透明，中间亮（紫色到白色）
        int[] colors = {
                Color.TRANSPARENT,
                0xFFC084FC,  // 柔和紫色
                0xFFE9D5FF,  // 浅紫色
                0xFFFFFFFF,  // 亮白色
                0xFFE9D5FF,
                0xFFC084FC,
                Color.TRANSPARENT
        };
        float[] positions = {0f, 0.2f, 0.35f, 0.5f, 0.65f, 0.8f, 1f};

        LinearGradient gradient = new LinearGradient(
                gradientStartX, gradientStartY,
                gradientEndX, gradientEndY,
                colors, positions,
                Shader.TileMode.CLAMP
        );

        // 5. 先绘制光晕（模糊效果）
        canvas.saveLayer(0, 0, getWidth(), getHeight(), null);

        // 光晕层：用半透明白色 + 模糊滤镜
        mGlowPaint.setShader(gradient);
        mGlowPaint.setMaskFilter(new BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL));
        mGlowPaint.setAlpha(100); // 半透明光晕
        canvas.drawPath(segment, mGlowPaint);

        // 6. 绘制主流光（清晰渐变）
        mShimmerPaint.setShader(gradient);
        mShimmerPaint.setMaskFilter(null);
        mShimmerPaint.setAlpha(255);
        canvas.drawPath(segment, mShimmerPaint);

        canvas.restore();

        // 清理引用，避免内存泄漏
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