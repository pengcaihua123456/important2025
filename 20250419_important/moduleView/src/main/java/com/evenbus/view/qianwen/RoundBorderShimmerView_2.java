package com.evenbus.view.qianwen;

import android.animation.ValueAnimator;
import android.content.Context;
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

/**
 * 原版本
 * 圆角矩形边框流光动画
 * 效果：光带长度 = 周长 1/4，顺时针沿边框移动，两端淡中间亮，移动变色
 */
public class RoundBorderShimmerView_2 extends View {
    // 卡片圆角、边框宽度
    private final float mCardRadius = 28f;
    private final float mStrokeWidth = 8f;

    // 边框路径 & 测量
    private Path mBorderPath = new Path();
    private PathMeasure mPathMeasure = new PathMeasure();

    // 流光固定长度 = 周长 1/4
    private float mShimmerLength;
    private float mAnimOffset = 0f;
    private ValueAnimator mAnimator;

    // 四组渐变颜色（经过四角自动切换）
    private int[][] colorGroups = {
            {Color.TRANSPARENT, 0xFF72D8FF, 0xFFFFFFFF, 0xFF72D8FF, Color.TRANSPARENT},
            {Color.TRANSPARENT, 0xFFC898FF, 0xFFFFFFFF, 0xFFC898FF, Color.TRANSPARENT},
            {Color.TRANSPARENT, 0xFFFFA8A8, 0xFFFFFFFF, 0xFFFFA8A8, Color.TRANSPARENT},
            {Color.TRANSPARENT, 0xFFA8FFC8, 0xFFFFFFFF, 0xFFA8FFC8, Color.TRANSPARENT}
    };
    private int mCurrentColorIndex = 0;

    // 画笔
    private Paint mBgPaint;
    private Paint mShimmerPaint;

    public RoundBorderShimmerView_2(Context context) {
        super(context);
        init();
    }

    public RoundBorderShimmerView_2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 黑色背景画笔
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

        // 构建圆角矩形路径（顺时针）
        RectF rectF = new RectF(
                mStrokeWidth / 2,
                mStrokeWidth / 2,
                w - mStrokeWidth / 2,
                h - mStrokeWidth / 2
        );
        mBorderPath.reset();
        mBorderPath.addRoundRect(rectF, mCardRadius, mCardRadius, Path.Direction.CW);

        // 测量周长，设置光长 = 1/4 周长
        mPathMeasure.setPath(mBorderPath, true);
        float totalLength = mPathMeasure.getLength();
        mShimmerLength = totalLength / 4f;

        // 无限循环动画
        mAnimator = ValueAnimator.ofFloat(0, totalLength);
        mAnimator.setDuration(3500);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mAnimOffset = (float) animation.getAnimatedValue();

            // 每移动 1/4 周长切换颜色
            float segment = totalLength / 4f;
            mCurrentColorIndex = (int) (mAnimOffset / segment) % colorGroups.length;

            invalidate();
        });
        mAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. 绘制黑色圆角卡片
        RectF cardRect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(cardRect, mCardRadius, mCardRadius, mBgPaint);

        // 2. 截取当前流光路径片段
        Path shimmerPath = new Path();
        mPathMeasure.getSegment(mAnimOffset, mAnimOffset + mShimmerLength, shimmerPath, true);

        // 3. 设置渐变：两端淡、中间亮
        LinearGradient gradient = new LinearGradient(
                0, 0, mShimmerLength, 0,
                colorGroups[mCurrentColorIndex],
                null,
                Shader.TileMode.CLAMP
        );
        mShimmerPaint.setShader(gradient);

        // 4. 绘制流光
        canvas.drawPath(shimmerPath, mShimmerPaint);
    }

    // 页面销毁时停止动画，防止内存泄漏
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }
}