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

public class RoundBorderShimmerView_6 extends View {
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

    private LinearGradient mFixedGradient;  // 固定渐变，不随动画改变

    public RoundBorderShimmerView_6(Context context) {
        super(context);
        init();
    }

    public RoundBorderShimmerView_6(Context context, AttributeSet attrs) {
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

        // 创建固定渐变（基于整个卡片边界，永不改变）
        createFixedGradient(w, h);

        startAnimation();
    }

    /**
     * 创建固定渐变，基于整个卡片对角线方向
     * 这样渐变方向始终不变，圆角处也能平滑过渡
     */
    private void createFixedGradient(int width, int height) {
        // 使用对角线方向（左上到右下），所有方向都有渐变变化
        float startX = 0f;
        float startY = 0f;
        float endX = width;
        float endY = height;

        // 渐变色：两端透明，中间亮
        int[] colors = {
                Color.TRANSPARENT,
                0xFFA855F7,  // 紫色
                0xFFC084FC,  // 浅紫
                0xFFE9D5FF,  // 更浅紫
                0xFFFFFFFF,  // 亮白中心
                0xFFE9D5FF,
                0xFFC084FC,
                0xFFA855F7,
                Color.TRANSPARENT
        };
        float[] positions = {0f, 0.15f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.85f, 1f};

        mFixedGradient = new LinearGradient(
                startX, startY, endX, endY,
                colors, positions,
                Shader.TileMode.CLAMP
        );
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

        // 3. 绘制光晕层（模糊 + 固定渐变）
        canvas.saveLayer(0, 0, getWidth(), getHeight(), null);

        mGlowPaint.setShader(mFixedGradient);
        mGlowPaint.setMaskFilter(new BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL));
        mGlowPaint.setAlpha(80);  // 半透明光晕
        canvas.drawPath(segment, mGlowPaint);

        // 4. 绘制主流光层（清晰 + 固定渐变）
        mShimmerPaint.setShader(mFixedGradient);
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