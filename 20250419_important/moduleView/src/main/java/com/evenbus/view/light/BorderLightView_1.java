package com.evenbus.view.light;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;


/***
 * 重要的效果
 */
public class BorderLightView_1 extends View {

    private Paint borderPaint;
    private Paint lightPaint;
    private RectF borderRect;
    private Path borderPath;
    private Path lightSegmentPath; // 新增：用于存储光段路径
    private PathMeasure pathMeasure;
    private float borderWidth = 10f;
    private float cornerRadius = 40f;
    private float progress = 0f; // 动画进度 0~1
    private ValueAnimator pathAnimator;
    private int[] lightColors = {Color.TRANSPARENT, Color.YELLOW, Color.TRANSPARENT};
    private LinearGradient linearGradient;
    private float pathLength = 0f;
    private float segmentLength = 0f; // 光段的长度
    private float segmentRatio = 0.125f; // 光段占路径总长度的比例，默认1/8
    private float startX, startY, endX, endY;
    private long animationDuration = 3000L; // 动画持续时间（毫秒）

    public BorderLightView_1(Context context) {
        this(context, null);
    }

    public BorderLightView_1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderLightView_1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(Color.parseColor("#333333"));

        lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightPaint.setStyle(Paint.Style.STROKE);
        lightPaint.setStrokeWidth(borderWidth);
        lightPaint.setStrokeCap(Paint.Cap.ROUND);

        borderRect = new RectF();
        borderPath = new Path();
        lightSegmentPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = borderWidth / 2;
        float dp30 = 30 * getResources().getDisplayMetrics().density;
        float bottom = h - padding - dp30;
        // 确保bottom不小于top
        if (bottom < padding) {
            bottom = padding + 1;
        }
        borderRect.set(padding, padding, w - padding, bottom);
        borderPath.reset();
        borderPath.addRoundRect(borderRect, cornerRadius, cornerRadius, Path.Direction.CW);

        // 初始化路径测量
        pathMeasure = new PathMeasure(borderPath, false);
        pathLength = pathMeasure.getLength();
        segmentLength = pathLength * segmentRatio; // 光段长度为总长度的比例

        // 初始计算一次光段位置
        updateLightSegment();
    }

    private void updateLightSegment() {
        if (pathMeasure == null || pathLength == 0) return;

        // 计算起点距离
        float startDistance = progress * pathLength;
        // 终点距离，取模确保在路径范围内
        float endDistance = startDistance + segmentLength;
        boolean wrap = false;
        if (endDistance > pathLength) {
            endDistance = endDistance % pathLength;
            wrap = true;
        }

        // 获取起点坐标（用于渐变）
        float[] startPos = new float[2];
        float[] startTan = new float[2];
        boolean startFound = pathMeasure.getPosTan(startDistance, startPos, startTan);
        if (startFound) {
            startX = startPos[0];
            startY = startPos[1];
        }

        // 获取终点坐标（用于渐变）
        float[] endPos = new float[2];
        float[] endTan = new float[2];
        boolean endFound = pathMeasure.getPosTan(endDistance, endPos, endTan);
        if (endFound) {
            endX = endPos[0];
            endY = endPos[1];
        }

        // 提取路径段
        lightSegmentPath.reset();
        if (!wrap) {
            // 未跨越起点，直接获取一段
            pathMeasure.getSegment(startDistance, endDistance, lightSegmentPath, true);
        } else {
            // 跨越起点，需要获取两段
            pathMeasure.getSegment(startDistance, pathLength, lightSegmentPath, true);
            // 需要重新测量路径以获取从0开始的部分
            PathMeasure tempMeasure = new PathMeasure(borderPath, false);
            tempMeasure.getSegment(0, endDistance, lightSegmentPath, true);
        }

        // 更新线性渐变
        updateGradient();
    }

    private void updateGradient() {
        if (pathMeasure == null || pathLength == 0) return;
        linearGradient = new LinearGradient(startX, startY, endX, endY, lightColors, null, android.graphics.Shader.TileMode.CLAMP);
        lightPaint.setShader(linearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制背景
        canvas.drawColor(Color.BLACK);

        // 绘制静态边框
        canvas.drawPath(borderPath, borderPaint);

        // 绘制流光段
        if (pathMeasure != null && pathLength > 0) {
            // 更新流光段位置
            updateLightSegment();
            // 绘制流光线段（现在使用路径）
            canvas.drawPath(lightSegmentPath, lightPaint);
        }
    }

    public void startAnimation() {
        startAnimation(animationDuration);
    }

    public void startAnimation(long duration) {
        this.animationDuration = duration;
        if (pathAnimator != null && pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        pathAnimator = ValueAnimator.ofFloat(0f, 1f);
        pathAnimator.setDuration(duration);
        pathAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pathAnimator.setInterpolator(new LinearInterpolator());
        pathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        pathAnimator.start();
    }

    public void stopAnimation() {
        if (pathAnimator != null) {
            pathAnimator.cancel();
            pathAnimator = null;
        }
    }

    public void setAnimationDuration(long duration) {
        this.animationDuration = duration;
        if (pathAnimator != null && pathAnimator.isRunning()) {
            // 重新启动动画以应用新的持续时间
            stopAnimation();
            startAnimation(duration);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    public void setBorderWidth(float width) {
        this.borderWidth = width;
        borderPaint.setStrokeWidth(width);
        lightPaint.setStrokeWidth(width);
        requestLayout();
    }

    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }

    public void setLightArcWidth(float degrees) {
        // 将角度（0~180）映射到比例（0.05~0.3）
        float ratio = 0.05f + (degrees / 180f) * 0.25f;
        this.segmentRatio = ratio;
        if (pathLength > 0) {
            segmentLength = pathLength * segmentRatio;
        }
        invalidate();
    }
}