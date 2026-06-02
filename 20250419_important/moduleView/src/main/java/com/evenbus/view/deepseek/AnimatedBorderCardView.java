package com.evenbus.view.deepseek;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 带有顺时针移动光效边框的圆角矩形卡片View
 * 使用 LinearGradient 实现渐变效果
 */
public class AnimatedBorderCardView extends View {

    private static final float DEFAULT_RADIUS_DP = 16f;
    private static final float EFFECT_LENGTH_RATIO = 0.25f;
    private static final long ANIMATION_DURATION = 3000;

    private Paint cardPaint;
    private Paint borderPaint;
    private Path cardPath;
    private PathMeasure pathMeasure;
    private float pathLength;

    private float currentOffset;
    private ValueAnimator borderAnimator;

    private int cardColor = Color.parseColor("#1E1E1E");

    // 渐变颜色组
    private static class GradientPair {
        int startColor;
        int endColor;
        GradientPair(int startColor, int endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
        }
    }

    private List<GradientPair> gradientGroups = new ArrayList<>();
    private int currentGroupIndex = 0;

    // 用于绘制光效片段的临时变量
    private Path effectPathSegment = new Path();
    private float[] tempPos = new float[2];
    private float[] tempTan = new float[2];

    public AnimatedBorderCardView(Context context) {
        this(context, null);
    }

    public AnimatedBorderCardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedBorderCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGradientGroups();
        init();
    }

    private void initGradientGroups() {
        gradientGroups.add(new GradientPair(
                Color.parseColor("#00FFD1"),  // 青绿色
                Color.parseColor("#A855F7")   // 紫色
        ));
        gradientGroups.add(new GradientPair(
                Color.parseColor("#FF6B6B"),  // 珊瑚红
                Color.parseColor("#FFD700")   // 金色
        ));
        gradientGroups.add(new GradientPair(
                Color.parseColor("#4FACFE"),  // 天蓝色
                Color.parseColor("#00F2FE")   // 青色
        ));
        gradientGroups.add(new GradientPair(
                Color.parseColor("#FA709A"),  // 粉红色
                Color.parseColor("#FEE140")   // 亮黄色
        ));
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setColor(cardColor);
        cardPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6f * density);
        borderPaint.setStrokeCap(Paint.Cap.ROUND);
        borderPaint.setStrokeJoin(Paint.Join.ROUND);
        borderPaint.setAntiAlias(true);

        cardPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateCardPath(w, h);
        startBorderAnimation();
    }

    private void updateCardPath(int width, int height) {
        if (width <= 0 || height <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        float radius = DEFAULT_RADIUS_DP * density;
        float padding = borderPaint.getStrokeWidth() / 2f;

        RectF rect = new RectF(padding, padding, width - padding, height - padding);
        cardPath.reset();
        cardPath.addRoundRect(rect, radius, radius, Path.Direction.CW);

        pathMeasure = new PathMeasure(cardPath, false);
        pathLength = pathMeasure.getLength();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(cardPath, cardPaint);

        if (pathLength > 0 && pathMeasure != null) {
            drawMovingEffect(canvas);
        }
    }

    private void drawMovingEffect(Canvas canvas) {
        float effectLen = pathLength * EFFECT_LENGTH_RATIO;
        if (effectLen <= 0) return;

        float startPos = currentOffset;
        float endPos = currentOffset + effectLen;

        if (endPos <= pathLength) {
            drawEffectSegment(canvas, startPos, endPos);
        } else {
            drawEffectSegment(canvas, startPos, pathLength);
            drawEffectSegment(canvas, 0, endPos - pathLength);
        }
    }

    /**
     * 使用 LinearGradient 绘制光效片段
     */
    private void drawEffectSegment(Canvas canvas, float start, float stop) {
        if (start >= stop) return;

        // 获取片段起点和终点的坐标
        pathMeasure.getPosTan(start, tempPos, null);
        float startX = tempPos[0];
        float startY = tempPos[1];

        pathMeasure.getPosTan(stop, tempPos, null);
        float stopX = tempPos[0];
        float stopY = tempPos[1];

        // 获取当前渐变色组
        GradientPair currentGroup = gradientGroups.get(currentGroupIndex);

        // 创建线性渐变（从起点颜色到终点颜色）
        LinearGradient gradient = new LinearGradient(
                startX, startY,
                stopX, stopY,
                currentGroup.startColor,
                currentGroup.endColor,
                Shader.TileMode.CLAMP
        );

        borderPaint.setShader(gradient);

        // 获取路径片段并绘制
        effectPathSegment.reset();
        pathMeasure.getSegment(start, stop, effectPathSegment, true);
        canvas.drawPath(effectPathSegment, borderPaint);

        // 清除 shader，避免影响后续绘制
        borderPaint.setShader(null);
    }

    private void startBorderAnimation() {
        if (borderAnimator != null && borderAnimator.isRunning()) {
            borderAnimator.cancel();
        }

        if (pathLength <= 0) return;

        borderAnimator = ValueAnimator.ofFloat(0f, pathLength);
        borderAnimator.setDuration(ANIMATION_DURATION);
        borderAnimator.setInterpolator(new LinearInterpolator());
        borderAnimator.setRepeatCount(ValueAnimator.INFINITE);
        borderAnimator.addUpdateListener(animation -> {
            float newOffset = (float) animation.getAnimatedValue();
            checkAndSwitchGradient(newOffset);
            currentOffset = newOffset;
            invalidate();
        });
        borderAnimator.start();
    }

    /**
     * 检测光效头部是否经过角点，切换渐变色组
     */
    private void checkAndSwitchGradient(float newOffset) {
        float oldOffset = currentOffset;
        float[] cornerPositions = getCornerPositions();

        for (float cornerPos : cornerPositions) {
            boolean crossedCorner = false;

            if (oldOffset < newOffset) {
                if (oldOffset <= cornerPos && newOffset > cornerPos) {
                    crossedCorner = true;
                }
            } else if (oldOffset > newOffset) {
                if (oldOffset <= cornerPos && cornerPos <= pathLength) {
                    crossedCorner = true;
                } else if (newOffset > cornerPos && cornerPos >= 0) {
                    crossedCorner = true;
                }
            }

            if (crossedCorner) {
                currentGroupIndex = (currentGroupIndex + 1) % gradientGroups.size();
                break;
            }
        }
    }

    /**
     * 获取4个角点的位置
     */
    private float[] getCornerPositions() {
        float[] corners = new float[4];
        if (pathMeasure == null || pathLength <= 0) return corners;

        // 采样找到每个四分之一长度的点（简化的角点检测）
        float quarterLen = pathLength / 4;
        for (int i = 0; i < 4; i++) {
            corners[i] = quarterLen * i;
        }

        return corners;
    }

    /**
     * 设置渐变色组
     */
    public void setGradientGroups(List<int[]> gradientPairs) {
        gradientGroups.clear();
        for (int[] pair : gradientPairs) {
            if (pair.length >= 2) {
                gradientGroups.add(new GradientPair(pair[0], pair[1]));
            }
        }
        if (gradientGroups.isEmpty()) {
            initGradientGroups();
        }
        invalidate();
    }

    /**
     * 设置卡片背景颜色
     */
    public void setCardBackgroundColor(@ColorInt int color) {
        this.cardColor = color;
        cardPaint.setColor(color);
        invalidate();
    }

    /**
     * 设置动画持续时间
     */
    public void setAnimationDuration(long durationMs) {
        if (borderAnimator != null) {
            borderAnimator.setDuration(durationMs);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (borderAnimator != null) {
            borderAnimator.cancel();
            borderAnimator = null;
        }
    }
}