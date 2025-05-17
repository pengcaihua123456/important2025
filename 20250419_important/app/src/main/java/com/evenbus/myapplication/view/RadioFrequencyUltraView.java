package com.evenbus.myapplication.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * @Author pengcaihua
 * @Date 13:20
 * @describe最高配的版本
 */
public class RadioFrequencyUltraView extends View {

    // 频率范围
    private static final float MIN_FREQUENCY = 87.5f;
    private static final float MAX_FREQUENCY = 108.0f;
    private float currentFrequency = 92.0f;

    // 绘制工具
    private Paint linePaint;
    private Paint textPaint;
    private Paint indicatorPaint;
    private Paint overscrollPaint;

    // 尺寸参数
    private int padding = 20;
    private int majorLineHeight = 30;
    private int minorLineHeight = 16;
    private int indicatorHeight = 40;
    private int textMargin = 20;

    // 触摸控制
    private float lastTouchX;
    private boolean isScaling = false;
    private VelocityTracker velocityTracker;
    private float lastVelocity;
    private static final float FLING_THRESHOLD = 1000f;
    private static final float DECELERATION_RATE = 0.9f;

    // 回弹效果
    private static final float OVERSCROLL_DAMPING = 0.3f;
    private static final float OVERSCROLL_RATIO = 0.2f;
    private float overscrollDistance;
    private boolean isOverscrolling = false;

    public RadioFrequencyUltraView(Context context) {
        super(context);
        init();
    }

    public RadioFrequencyUltraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadioFrequencyUltraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 刻度线画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(dpToPx(1));

        // 文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(spToPx(12));
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 指示器画笔
        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setColor(Color.RED);
        indicatorPaint.setStrokeWidth(dpToPx(3));

        // 回弹效果画笔
        overscrollPaint = new Paint();
        overscrollPaint.setColor(Color.parseColor("#33FF0000")); // 半透明红色
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = dpToPx(80); // 默认高度

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float centerY = height / 2f;

        // 绘制背景
        canvas.drawColor(Color.WHITE);

        // 计算基础偏移量
        float frequencyRange = MAX_FREQUENCY - MIN_FREQUENCY;
        float frequencyRatio = (currentFrequency - MIN_FREQUENCY) / frequencyRange;
        float baseOffset = width / 2f - frequencyRatio * width;

        // 应用回弹偏移
        float totalOffset = baseOffset;
        if (overscrollDistance != 0) {
            totalOffset += overscrollDistance / frequencyRange * width;

            // 绘制回弹指示
            if (overscrollDistance < 0) {
                // 左边界回弹
                canvas.drawRect(0, 0, dpToPx(10), height, overscrollPaint);
            } else {
                // 右边界回弹
                canvas.drawRect(width - dpToPx(10), 0, width, height, overscrollPaint);
            }
        }

        // 绘制主刻度线（1MHz间隔）
        float majorStep = 1.0f;
        for (float freq = MIN_FREQUENCY; freq <= MAX_FREQUENCY; freq += majorStep) {
            float x = totalOffset + (freq - MIN_FREQUENCY) / frequencyRange * width;

            if (x >= -100 && x <= width + 100) {
                // 主刻度线
                canvas.drawLine(x, centerY - dpToPx(majorLineHeight/2),
                        x, centerY + dpToPx(majorLineHeight/2), linePaint);

                // 主刻度标签
                String label = String.format("%.0f", freq);
                canvas.drawText(label, x, centerY + dpToPx(majorLineHeight/2 + textMargin), textPaint);
            }
        }

        // 绘制次刻度线（0.1MHz间隔）
        float minorStep = 0.1f;
        for (float freq = MIN_FREQUENCY; freq <= MAX_FREQUENCY; freq += minorStep) {
            // 跳过主刻度点
            if (freq % majorStep == 0) continue;

            float x = totalOffset + (freq - MIN_FREQUENCY) / frequencyRange * width;

            if (x >= -100 && x <= width + 100) {
                canvas.drawLine(x, centerY - dpToPx(minorLineHeight/2),
                        x, centerY + dpToPx(minorLineHeight/2), linePaint);
            }
        }

        // 绘制指示器（始终居中）
        float indicatorX = width / 2f;
        canvas.drawLine(indicatorX, centerY - dpToPx(indicatorHeight/2),
                indicatorX, centerY + dpToPx(indicatorHeight/2), indicatorPaint);

        // 绘制当前频率显示
        String currentText = String.format("%.1f MHz", currentFrequency);
        canvas.drawText(currentText, indicatorX, centerY - dpToPx(indicatorHeight/2 + textMargin), textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;

            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleActionUp();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handleActionDown(MotionEvent event) {
        removeCallbacks(flingRunnable);
        lastTouchX = event.getX();
        isScaling = true;
        isOverscrolling = false;
    }

    private void handleActionMove(MotionEvent event) {
        if (isScaling) {
            float dx = event.getX() - lastTouchX;
            lastTouchX = event.getX();

            // 计算频率变化量
            float deltaFreq = -dx / getWidth() * (MAX_FREQUENCY - MIN_FREQUENCY);
            adjustFrequencyWithOverscroll(deltaFreq);
        }
    }

    private void handleActionUp() {
        isScaling = false;

        // 计算滑动速度
        velocityTracker.computeCurrentVelocity(1000);
        lastVelocity = velocityTracker.getXVelocity();

        // 启动惯性滑动或回弹动画
        if (overscrollDistance != 0 || Math.abs(lastVelocity) > FLING_THRESHOLD) {
            post(flingRunnable);
        }

        // 回收VelocityTracker
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void adjustFrequencyWithOverscroll(float deltaFrequency) {
        float newFrequency = currentFrequency + deltaFrequency;

        // 检查是否超出边界
        if (newFrequency < MIN_FREQUENCY) {
            overscrollDistance = (newFrequency - MIN_FREQUENCY) * OVERSCROLL_RATIO;
            isOverscrolling = true;
        } else if (newFrequency > MAX_FREQUENCY) {
            overscrollDistance = (newFrequency - MAX_FREQUENCY) * OVERSCROLL_RATIO;
            isOverscrolling = true;
        } else {
            overscrollDistance = 0;
            isOverscrolling = false;
        }

        // 更新频率
        currentFrequency = isOverscrolling ?
                (newFrequency < MIN_FREQUENCY ? MIN_FREQUENCY : MAX_FREQUENCY) + overscrollDistance :
                newFrequency;

        invalidate();
    }

    private Runnable flingRunnable = new Runnable() {
        @Override
        public void run() {
            // 优先处理回弹
            if (overscrollDistance != 0) {
                overscrollDistance *= OVERSCROLL_DAMPING;

                if (Math.abs(overscrollDistance) < 0.1f) {
                    overscrollDistance = 0;
                }

                currentFrequency = currentFrequency < MIN_FREQUENCY ?
                        MIN_FREQUENCY + overscrollDistance :
                        MAX_FREQUENCY + overscrollDistance;

                invalidate();

                if (overscrollDistance != 0) {
                    postDelayed(this, 16);
                }
                return;
            }

            // 处理惯性滑动
            if (Math.abs(lastVelocity) < 50) {
                return;
            }

            // 调整频率（注意方向取反）
            adjustFrequencyWithOverscroll(-lastVelocity / 60 / getWidth() * (MAX_FREQUENCY - MIN_FREQUENCY));

            // 减速
            lastVelocity *= DECELERATION_RATE;

            postDelayed(this, 16);
        }
    };

    public void setCurrentFrequency(float frequency) {
        frequency = Math.max(MIN_FREQUENCY, Math.min(MAX_FREQUENCY, frequency));
        if (currentFrequency != frequency) {
            currentFrequency = frequency;
            invalidate();
        }
    }

    public float getCurrentFrequency() {
        return currentFrequency;
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int spToPx(float sp) {
        return (int) (sp * getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }
}