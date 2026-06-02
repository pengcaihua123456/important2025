package com.evenbus.view.texture;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class RoundBorderShimmerView_Shader_2 extends View {
    private final float mCardRadius = 28f;
    private final float mStrokeWidth = 6f;

    private Paint mBgPaint;
    private Paint mBorderPaint;
    private Path mBorderPath;
    private float mTotalLength;
    private float mAnimOffset = 0f;
    private ValueAnimator mAnimator;

    // 自定义 Shader 相关
    private Bitmap mGradientBitmap;
    private BitmapShader mBitmapShader;
    private Matrix mShaderMatrix;
    private RectF mCardRect;

    public RoundBorderShimmerView_Shader_2(Context context) {
        super(context);
        init();
    }

    public RoundBorderShimmerView_Shader_2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(0xFF121212);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mStrokeWidth);

        mShaderMatrix = new Matrix();
        mBorderPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 计算边框路径和周长
        mCardRect = new RectF(
                mStrokeWidth / 2,
                mStrokeWidth / 2,
                w - mStrokeWidth / 2,
                h - mStrokeWidth / 2
        );
        mBorderPath.reset();
        mBorderPath.addRoundRect(mCardRect, mCardRadius, mCardRadius, Path.Direction.CW);

        PathMeasure pm = new PathMeasure(mBorderPath, true);
        mTotalLength = pm.getLength();

        // 创建渐变纹理
        createGradientTexture();

        startAnimation();
    }

    private void createGradientTexture() {
        // 纹理宽度：路径周长，纹理高度：1px
        int textureWidth = (int) mTotalLength;
        int textureHeight = 1;

        // 限制最大纹理尺寸（避免过大）
        if (textureWidth > 2048) {
            textureWidth = 2048;
        }

        mGradientBitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mGradientBitmap);

        // 创建沿路径方向的渐变（透明 -> 淡紫 -> 亮白 -> 淡紫 -> 透明）
        int[] colors = {
                Color.TRANSPARENT,
                0xFFE0B0FF,
                0xFFFFFFFF,
                0xFFE0B0FF,
                Color.TRANSPARENT
        };
        float[] positions = {0f, 0.25f, 0.5f, 0.75f, 1f};

        // 流光段的长度占总周长的 1/4
        float shimmerLength = mTotalLength / 4f;

        // 为整个纹理创建渐变（包含多个循环周期）
        Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // 我们创建一个包含两个完整流光段的纹理，方便做无缝循环
        int cycleWidth = (int) mTotalLength;
        for (int offset = 0; offset < textureWidth; offset += cycleWidth) {
            LinearGradient gradient = new LinearGradient(
                    offset, 0,
                    offset + shimmerLength, 0,
                    colors, positions,
                    Shader.TileMode.CLAMP
            );
            gradientPaint.setShader(gradient);
            canvas.drawRect(offset, 0, offset + shimmerLength, textureHeight, gradientPaint);
        }

        // 创建 BitmapShader
        mBitmapShader = new BitmapShader(mGradientBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
    }

    private void startAnimation() {
        mAnimator = ValueAnimator.ofFloat(0, mTotalLength);
        mAnimator.setDuration(4000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mAnimOffset = (float) animation.getAnimatedValue();

            // 更新 Shader 的偏移量
            if (mBitmapShader != null) {
                mShaderMatrix.reset();
                mShaderMatrix.setTranslate(-mAnimOffset, 0);
                mBitmapShader.setLocalMatrix(mShaderMatrix);
            }
            invalidate();
        });
        mAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 画背景
        canvas.drawRoundRect(mCardRect, mCardRadius, mCardRadius, mBgPaint);

        // 用自定义 Shader 画边框
        mBorderPaint.setShader(mBitmapShader);
        canvas.drawPath(mBorderPath, mBorderPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) mAnimator.cancel();
        if (mGradientBitmap != null) {
            mGradientBitmap.recycle();
        }
    }
}
