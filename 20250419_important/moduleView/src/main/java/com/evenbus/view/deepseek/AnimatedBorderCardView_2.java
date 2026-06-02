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
 * 光效长度为周长的1/4，两种颜色渐变，两侧淡中间亮
 * 每经过一个角，渐变色会变化一次（共有4组颜色循环）
 */
public class AnimatedBorderCardView_2 extends View {

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

    // 渐变颜色组（每组包含起始色和结束色）
    private static class GradientPair {
        int startColor;
        int endColor;
        GradientPair(int startColor, int endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
        }
    }

    // 默认4组渐变色（可以根据需要修改）
    private List<GradientPair> gradientGroups = new ArrayList<>();
    private int currentGroupIndex = 0;  // 当前使用哪一组渐变
    private float progressInRound = 0f; // 当前圈内进度 0~1，用于检测经过角

    public AnimatedBorderCardView_2(Context context) {
        this(context, null);
    }

    public AnimatedBorderCardView_2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedBorderCardView_2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGradientGroups();
        init();
    }

    private void initGradientGroups() {
        // 默认4组渐变色，经过4个角（完整一圈）后循环
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
            drawGradientOnPath(canvas, startPos, endPos);
        } else {
            drawGradientOnPath(canvas, startPos, pathLength);
            drawGradientOnPath(canvas, 0, endPos - pathLength);
        }
    }

    private void drawGradientOnPath(Canvas canvas, float start, float stop) {
        if (start >= stop) return;
        float segmentLen = stop - start;

        float step = Math.max(1f, segmentLen / 100f);

        float totalEffectLen = pathLength * EFFECT_LENGTH_RATIO;

        for (float distOnPath = start; distOnPath <= stop; distOnPath += step) {
            float effectPos;
            if (distOnPath >= currentOffset) {
                effectPos = (distOnPath - currentOffset) / totalEffectLen;
            } else {
                effectPos = (distOnPath + pathLength - currentOffset) / totalEffectLen;
            }
            effectPos = Math.min(1f, Math.max(0f, effectPos));

            // 计算透明度：两端淡，中间亮
            float alphaFactor;
            if (effectPos <= 0.5f) {
                alphaFactor = effectPos / 0.5f;
            } else {
                alphaFactor = (1.0f - effectPos) / 0.5f;
            }
            int alpha = (int) (50 + alphaFactor * 205);

            // 获取当前组渐变色
            GradientPair currentGroup = gradientGroups.get(currentGroupIndex);
            int baseColor = interpolateColor(currentGroup.startColor, currentGroup.endColor, effectPos);
            int argbColor = (alpha << 24) | (baseColor & 0x00FFFFFF);

            float[] pos = new float[2];
            boolean success = pathMeasure.getPosTan(distOnPath, pos, null);
            if (!success) continue;

            borderPaint.setColor(argbColor);
            canvas.drawPoint(pos[0], pos[1], borderPaint);
        }
    }

    private int interpolateColor(int colorA, int colorB, float t) {
        int rA = (colorA >> 16) & 0xFF;
        int gA = (colorA >> 8) & 0xFF;
        int bA = colorA & 0xFF;
        int rB = (colorB >> 16) & 0xFF;
        int gB = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;
        int r = (int) (rA + (rB - rA) * t);
        int g = (int) (gA + (gB - gA) * t);
        int b = (int) (bA + (bB - bA) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
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

            // 检测是否经过角（一圈有4个角，每经过一个角切换一次渐变色）
            checkAndSwitchGradient(newOffset);

            currentOffset = newOffset;
            invalidate();
        });
        borderAnimator.start();
    }

    /**
     * 检测光效头部是否经过角，每经过一个角切换一组渐变色
     * @param newOffset 新的偏移量
     */
    private void checkAndSwitchGradient(float newOffset) {
        // 计算上一帧的位置（用于判断是否跨过角点）
        float oldOffset = currentOffset;

        // 获取所有角点的位置（圆角矩形的4个角）
        float[] cornerPositions = getCornerPositions();

        for (float cornerPos : cornerPositions) {
            // 判断光效头部是否跨过了这个角点
            boolean crossedCorner = false;

            if (oldOffset < newOffset) {
                // 正常向前移动，没有环绕
                if (oldOffset <= cornerPos && newOffset > cornerPos) {
                    crossedCorner = true;
                }
            } else if (oldOffset > newOffset) {
                // 发生了环绕（跨过终点回到起点）
                if (oldOffset <= cornerPos && cornerPos <= pathLength) {
                    crossedCorner = true;
                } else if (newOffset > cornerPos && cornerPos >= 0) {
                    crossedCorner = true;
                }
            }

            if (crossedCorner) {
                // 切换到下一组渐变色
                currentGroupIndex = (currentGroupIndex + 1) % gradientGroups.size();
                break;
            }
        }
    }

    /**
     * 获取圆角矩形4个角点的位置（顺时针方向）
     * 顺序：左上角 -> 右上角 -> 右下角 -> 左下角
     */
    private float[] getCornerPositions() {
        float[] corners = new float[4];
        if (pathMeasure == null || pathLength <= 0) return corners;

        // 通过采样路径找到角点位置
        // 角点处曲率变化最大，简单方法：测量每个四分之一长度的大概位置
        float quarterLen = pathLength / 4;
        for (int i = 0; i < 4; i++) {
            corners[i] = quarterLen * i;
        }

        // 更精确的方法：采样路径，找到距离矩形角点最近的位置
        float density = getResources().getDisplayMetrics().density;
        float radius = DEFAULT_RADIUS_DP * density;
        float strokeHalf = borderPaint.getStrokeWidth() / 2f;

        // 获取View尺寸
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return corners;

        // 矩形四个角点的实际坐标（不考虑圆角）
        float left = strokeHalf;
        float top = strokeHalf;
        float right = width - strokeHalf;
        float bottom = height - strokeHalf;

        PointF[] rectCorners = {
                new PointF(left + radius, top),           // 右上角起点
                new PointF(right, top + radius),          // 右上角
                new PointF(right, bottom - radius),       // 右下角
                new PointF(left + radius, bottom)         // 左下角
        };

        // 找到路径上最接近这些角点的位置
        float step = pathLength / 200f;
        for (int i = 0; i < 4; i++) {
            float minDist = Float.MAX_VALUE;
            float bestPos = corners[i];

            for (float pos = 0; pos <= pathLength; pos += step) {
                float[] point = new float[2];
                if (pathMeasure.getPosTan(pos, point, null)) {
                    float dx = point[0] - rectCorners[i].x;
                    float dy = point[1] - rectCorners[i].y;
                    float dist = dx * dx + dy * dy;
                    if (dist < minDist) {
                        minDist = dist;
                        bestPos = pos;
                    }
                }
            }
            corners[i] = bestPos;
        }

        return corners;
    }

    /**
     * 设置自定义的渐变色组（每组包含起始色和结束色）
     * @param gradientPairs 渐变色组列表，每组包含起始色和结束色
     */
    public void setGradientGroups(List<int[]> gradientPairs) {
        gradientGroups.clear();
        for (int[] pair : gradientPairs) {
            if (pair.length >= 2) {
                gradientGroups.add(new GradientPair(pair[0], pair[1]));
            }
        }
        if (gradientGroups.isEmpty()) {
            // 如果没有设置，使用默认值
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