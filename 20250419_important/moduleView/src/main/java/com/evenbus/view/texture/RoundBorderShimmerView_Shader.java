package com.evenbus.view.texture;

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

public class RoundBorderShimmerView_Shader extends View {
    private final float mCardRadius = 28f;
    private final float mStrokeWidth = 6f;
    private final float SHIMMER_RATIO = 0.25f; // 流光段占总周长的比例

    private Paint mBgPaint;
    private Paint mBorderPaint;  // 静态边框
    private Paint mShimmerPaint; // 流光画笔

    private Path mBorderPath;
    private Path mCurrentShimmerPath; // 当前要绘制的流光段路径
    private PathMeasure mPathMeasure;
    private float mTotalLength;
    private float mShimmerLength;
    private float mProgress = 0f; // 动画进度 0~1

    private ValueAnimator mAnimator;
    private RectF mCardRect;

    // 渐变相关
    private float mStartX, mStartY, mEndX, mEndY;
    private int[] mLightColors = {
            Color.TRANSPARENT,
            0xFFE0B0FF,
            0xFFFFFFFF,
            0xFFE0B0FF,
            Color.TRANSPARENT
    };
    private float[] mLightPositions = {0f, 0.25f, 0.5f, 0.75f, 1f};

    public RoundBorderShimmerView_Shader(Context context) {
        super(context);
        init();
    }

    public RoundBorderShimmerView_Shader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(0xFF121212);

        // 静态边框画笔
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mStrokeWidth);
        mBorderPaint.setColor(Color.parseColor("#333333"));

        // 流光画笔
        mShimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShimmerPaint.setStyle(Paint.Style.STROKE);
        mShimmerPaint.setStrokeWidth(mStrokeWidth);
        mShimmerPaint.setStrokeCap(Paint.Cap.ROUND);

        mBorderPath = new Path();
        mCurrentShimmerPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCardRect = new RectF(
                mStrokeWidth / 2,
                mStrokeWidth / 2,
                w - mStrokeWidth / 2,
                h - mStrokeWidth / 2
        );

        // 构建边框路径
        mBorderPath.reset();
        mBorderPath.addRoundRect(mCardRect, mCardRadius, mCardRadius, Path.Direction.CW);

        mPathMeasure = new PathMeasure(mBorderPath, true);
        mTotalLength = mPathMeasure.getLength();
        mShimmerLength = mTotalLength * SHIMMER_RATIO;

        startAnimation();
    }

    private void updateShimmerSegment() {
        if (mPathMeasure == null || mTotalLength == 0) return;

        // 计算流光段的起止距离
        float startDistance = mProgress * mTotalLength;
        float endDistance = startDistance + mShimmerLength;

        boolean wrap = endDistance > mTotalLength;

        // 获取起点和终点的坐标（用于 LinearGradient）
        float[] startPos = new float[2];
        float[] endPos = new float[2];
        float[] dummyTan = new float[2];

        if (wrap) {
            // 如果跨越终点，起点在 startDistance，终点在 endDistance - mTotalLength
            mPathMeasure.getPosTan(startDistance, startPos, dummyTan);
            mPathMeasure.getPosTan(endDistance - mTotalLength, endPos, dummyTan);
        } else {
            mPathMeasure.getPosTan(startDistance, startPos, dummyTan);
            mPathMeasure.getPosTan(endDistance, endPos, dummyTan);
        }

        mStartX = startPos[0];
        mStartY = startPos[1];
        mEndX = endPos[0];
        mEndY = endPos[1];

        // 提取流光段路径
        mCurrentShimmerPath.reset();
        if (!wrap) {
            mPathMeasure.getSegment(startDistance, endDistance, mCurrentShimmerPath, true);
        } else {
            // 跨越终点：第一段从 startDistance 到终点
            mPathMeasure.getSegment(startDistance, mTotalLength, mCurrentShimmerPath, true);
            // 第二段从起点到 endDistance - mTotalLength
            Path secondSegment = new Path();
            mPathMeasure.getSegment(0, endDistance - mTotalLength, secondSegment, true);
            mCurrentShimmerPath.addPath(secondSegment);
        }

        // 关键：创建动态的 LinearGradient，方向从起点到终点
        LinearGradient gradient = new LinearGradient(
                mStartX, mStartY,
                mEndX, mEndY,
                mLightColors, mLightPositions,
                Shader.TileMode.CLAMP
        );
        mShimmerPaint.setShader(gradient);
    }

    private void startAnimation() {
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setDuration(4000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 画背景
        canvas.drawRoundRect(mCardRect, mCardRadius, mCardRadius, mBgPaint);

        // 画静态边框
        canvas.drawPath(mBorderPath, mBorderPaint);

        // 更新并绘制流光段
        updateShimmerSegment();
        canvas.drawPath(mCurrentShimmerPath, mShimmerPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) mAnimator.cancel();
    }
}