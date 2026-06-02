package com.evenbus.view.light;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

public class GradientBorderView extends FrameLayout {
    //画笔、渐变、范围矩形
    private Paint mBorderPaint;
    private RectF mBorderRect;
    private float mRotateAngle = 0f; //旋转偏移角度（动画变量）
    private final float STROKE_WIDTH = 24f; //边框粗细
    private final float RADIUS = 24f;     //圆角大小
    private ObjectAnimator mAnimator;

    public GradientBorderView(Context context) {
        super(context);
        init();
    }
    public GradientBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Log.e("peng","init");
        setWillNotDraw(false); //开启onDraw绘制
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(STROKE_WIDTH);
        mBorderRect = new RectF();

        //属性动画：0~360°无限旋转，1s一圈
        mAnimator = ObjectAnimator.ofFloat(this, "rotateAngle", 0f, 360f);
        mAnimator.setDuration(1000);
        mAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        Log.e("peng", "Animation created");
    }

    //动画反射set方法，修改角度并重绘
    public void setRotateAngle(float angle) {
        Log.e("peng", "setRotateAngle: " + angle);
        this.mRotateAngle = angle;
        invalidate();
    }
    
    //动画反射get方法
    public float getRotateAngle() {
        return mRotateAngle;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("peng", "onSizeChanged: w=" + w + ", h=" + h);
        //内缩半个线宽，防止边框被裁切
        float halfStroke = STROKE_WIDTH / 2f;
        mBorderRect.set(halfStroke, halfStroke, w-halfStroke, h-halfStroke);
        Log.e("peng", "mBorderRect: " + mBorderRect.toString());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.e("peng", "onAttachedToWindow");
        if (mAnimator != null && !mAnimator.isStarted()) {
            Log.e("peng", "Starting animation...");
            mAnimator.start();
            Log.e("peng", "Animation started");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.e("peng", "onDetachedFromWindow");
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // FrameLayout的onDraw通常不绘制内容，我们将在dispatchDraw中绘制边框
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 先绘制子视图
        super.dispatchDraw(canvas);
        
        Log.e("peng", "dispatchDraw: mRotateAngle=" + mRotateAngle);
        
        //彩虹色渐变数组，更鲜艳
        int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.RED};
        //旋转渐变Shader，随mRotateAngle偏移实现流光跑动
        SweepGradient sweepGradient = new SweepGradient(
                getWidth()/2f, getHeight()/2f,
                colors, null
        );
        Matrix matrix = new Matrix();
        matrix.postRotate(mRotateAngle, getWidth()/2f, getHeight()/2f);
        sweepGradient.setLocalMatrix(matrix);
        mBorderPaint.setShader(sweepGradient);
        
        //绘制圆角矩形边框（在子视图之上）
        canvas.drawRoundRect(mBorderRect, RADIUS, RADIUS, mBorderPaint);
    }
}