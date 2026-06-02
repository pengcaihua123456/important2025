package com.evenbus.view.qianwen;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class RoundBorderShimmerView_3 extends View {
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

    public RoundBorderShimmerView_3(Context context) {
        super(context);
        init();
    }

    public RoundBorderShimmerView_3(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 黑色背景
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(0xFF121212);

        // 流光画笔
        mShimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShimmerPaint.setStyle(Paint.Style.STROKE);
        mShimmerPaint.setStrokeWidth(mStrokeWidth);
        mShimmerPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 边框路径（顺时针）
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

        // 2. 计算当前要显示的流光路径段
        float start = mAnimOffset;
        float end = start + mShimmerLength;

        if (end <= mTotalLength) {
            // 没有跨过终点
            Path segment = new Path();
            mPathMeasure.getSegment(start, end, segment, true);
            segment.rLineTo(0, 0);
            drawShimmerWithGradient(canvas, segment);
        } else {
            // 跨过终点，分两段绘制
            Path firstSegment = new Path();
            mPathMeasure.getSegment(start, mTotalLength, firstSegment, true);
            firstSegment.rLineTo(0, 0);
            drawShimmerWithGradient(canvas, firstSegment);

            Path secondSegment = new Path();
            mPathMeasure.getSegment(0, end - mTotalLength, secondSegment, true);
            secondSegment.rLineTo(0, 0);
            drawShimmerWithGradient(canvas, secondSegment);
        }
    }

    /**
     * 绘制带渐变效果的流光（沿路径方向）
     */
    private void drawShimmerWithGradient(Canvas canvas, Path path) {
        PathMeasure pm = new PathMeasure(path, false);
        float totalLen = pm.getLength();
        if (totalLen <= 0) return;

        // 渐变颜色：透明 -> 淡紫 -> 亮白 -> 淡紫 -> 透明
        int[] colors = {
                Color.TRANSPARENT,
                0xFFE0B0FF,
                0xFFFFFFFF,
                0xFFE0B0FF,
                Color.TRANSPARENT
        };
        float[] positions = {0f, 0.25f, 0.5f, 0.75f, 1f};

        // 分段绘制（每段约 6px，实现平滑的沿路径渐变）
        float stepSize = Math.max(3f, totalLen / 80f);
        int steps = (int) Math.ceil(totalLen / stepSize);

        Path segmentPath = new Path();

        for (int i = 0; i < steps; i++) {
            float segStart = i * stepSize;
            float segEnd = Math.min(segStart + stepSize, totalLen);
            if (segStart >= totalLen) break;

            // 计算当前段中心点在整条路径上的比例
            float midT = (segStart + segEnd) / 2 / totalLen;

            // 根据比例获取颜色
            int color = getGradientColor(colors, positions, midT);

            // 获取这一段路径
            segmentPath.reset();
            pm.getSegment(segStart, segEnd, segmentPath, true);

            // 绘制这一段
            mShimmerPaint.setShader(null);
            mShimmerPaint.setColor(color);
            canvas.drawPath(segmentPath, mShimmerPaint);
        }
    }

    /**
     * 获取渐变中某个比例的颜色值
     */
    private int getGradientColor(int[] colors, float[] positions, float t) {
        if (t <= positions[0]) return colors[0];
        if (t >= positions[positions.length - 1]) return colors[positions.length - 1];

        for (int i = 0; i < positions.length - 1; i++) {
            if (t >= positions[i] && t <= positions[i + 1]) {
                float ratio = (t - positions[i]) / (positions[i + 1] - positions[i]);
                return blendColors(colors[i], colors[i + 1], ratio);
            }
        }
        return colors[0];
    }

    /**
     * 混合两个颜色
     */
    private int blendColors(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 * (1 - ratio) + a2 * ratio);
        int r = (int) (r1 * (1 - ratio) + r2 * ratio);
        int g = (int) (g1 * (1 - ratio) + g2 * ratio);
        int b = (int) (b1 * (1 - ratio) + b2 * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) mAnimator.cancel();
    }
}