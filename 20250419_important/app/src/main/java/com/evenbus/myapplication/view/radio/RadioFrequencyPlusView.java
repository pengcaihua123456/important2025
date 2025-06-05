package com.evenbus.myapplication.view.radio;


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
 * @Date 10:44
 * @describe
 */

/**
 * 自定义收音机频率刻度视图
 * 功能：显示87.5MHz到108.0MHz的频率刻度，并将当前频率指示器居中显示
 * 1.需要拿到中间位置
 * 2.拿到起始点的坐标
 * 3.拿到当前频率的位置(没有每个步进的距离
 * 4.最终计算的是频率的x的位置
 */
public class RadioFrequencyPlusView extends View {

    // 频率范围
    private static final float MIN_FREQUENCY = 87.5f;
    private static final float MAX_FREQUENCY = 108.0f;
    private float currentFrequency = 92.0f;

    // 绘制工具
    private Paint linePaint;
    private Paint textPaint;
    private Paint indicatorPaint;

    // 触摸相关
    private float lastTouchX;
    private boolean isScaling = false;
    private VelocityTracker velocityTracker;
    private float lastVelocity;
    private static final float FLING_THRESHOLD = 1000f;
    private static final float DECELERATION_RATE = 0.9f;

    public RadioFrequencyPlusView(Context context) {
        this(context, null);
    }

    public RadioFrequencyPlusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioFrequencyPlusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(2);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(spToPx(12));

        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setColor(Color.RED);
        indicatorPaint.setStrokeWidth(4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = dpToPx(80);

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

        // 计算绘制参数
        float scaleLength = width;
        float frequencyRatio = (currentFrequency - MIN_FREQUENCY) / (MAX_FREQUENCY - MIN_FREQUENCY);
        float offset = width / 2f - frequencyRatio * scaleLength;

        // 绘制主刻度
        float majorStep = 1.0f;
        int majorCount = (int)((MAX_FREQUENCY - MIN_FREQUENCY) / majorStep) + 1;

        for (int i = 0; i < majorCount; i++) {
            float frequency = MIN_FREQUENCY + i * majorStep;
            float x = offset + (frequency - MIN_FREQUENCY) / (MAX_FREQUENCY - MIN_FREQUENCY) * scaleLength;

            if (x >= -100 && x <= width + 100) {
                canvas.drawLine(x, centerY - dpToPx(15), x, centerY + dpToPx(15), linePaint);

                String text = String.format("%.0f", frequency);
                float textWidth = textPaint.measureText(text);
                canvas.drawText(text, x - textWidth / 2, centerY + dpToPx(30), textPaint);
            }
        }

        // 绘制次刻度
        float minorStep = 0.1f;
        int minorCount = (int)((MAX_FREQUENCY - MIN_FREQUENCY) / minorStep) + 1;

        for (int i = 0; i < minorCount; i++) {
            float frequency = MIN_FREQUENCY + i * minorStep;
            float x = offset + (frequency - MIN_FREQUENCY) / (MAX_FREQUENCY - MIN_FREQUENCY) * scaleLength;

            if (x >= -100 && x <= width + 100 && i % 10 != 0) {
                canvas.drawLine(x, centerY - dpToPx(8), x, centerY + dpToPx(8), linePaint);
            }
        }

        // 绘制指示器
        float indicatorX = width / 2f;
        canvas.drawLine(indicatorX, centerY - dpToPx(20), indicatorX, centerY + dpToPx(20), indicatorPaint);

        // 绘制当前频率
        String currentText = String.format("%.1f MHz", currentFrequency);
        float textWidth = textPaint.measureText(currentText);
        canvas.drawText(currentText, indicatorX - textWidth / 2, centerY - dpToPx(25), textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 初始化速度跟踪器，用于计算滑动速度
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        // 将当前触摸事件添加到速度跟踪器中
        velocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时停止所有正在进行的惯性滑动动画
                removeCallbacks(flingRunnable);
                // 记录按下的X坐标
                lastTouchX = event.getX();
                // 标记开始滑动状态
                isScaling = true;
                return true; // 表示消费该事件

            case MotionEvent.ACTION_MOVE:
                if (isScaling) {
                    // 计算手指移动的距离（当前X坐标 - 上次X坐标）
                    float dx = event.getX() - lastTouchX;
                    // 更新上次X坐标为当前位置
                    lastTouchX = event.getX();

                    // 调整频率：
                    // 1. 将像素移动距离转换为频率变化量
                    // 2. dx为正表示手指向右滑动，此时刻度应向左移动（频率增加），所以取负值
                    // 3. 除以getWidth()将像素距离转换为比例
                    // 4. 乘以频率范围(MAX-MIN)得到实际频率变化量
                    adjustFrequencyByDelta(-dx / getWidth() * (MAX_FREQUENCY - MIN_FREQUENCY));
                }
                return true; // 消费移动事件

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 手指抬起或取消时，结束滑动状态
                isScaling = false;

                // 计算当前滑动速度（像素/秒）
                velocityTracker.computeCurrentVelocity(1000); // 1000表示单位为毫秒
                lastVelocity = velocityTracker.getXVelocity();

                // 如果速度超过阈值，启动惯性滑动动画
                if (Math.abs(lastVelocity) > FLING_THRESHOLD) {
                    post(flingRunnable);
                }

                // 回收速度跟踪器资源
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                return true; // 消费抬起/取消事件
        }
        return super.onTouchEvent(event); // 其他情况交给父类处理
    }


    private Runnable flingRunnable = new Runnable() {
        @Override
        public void run() {
            // 当速度小于阈值时停止惯性滑动
            if (Math.abs(lastVelocity) < 50) {
                return;
            }

            /**
             * 惯性滑动时调整频率：
             * 1. lastVelocity为正表示手指向右快速滑动，此时刻度应向左移动（频率增加），所以取负值
             * 2. 除以60将速度转换为每帧的位移（假设60fps）
             * 3. 除以getWidth()将像素距离转换为比例
             * 4. 乘以频率范围(MAX-MIN)得到实际频率变化量
             */
            adjustFrequencyByDelta(-lastVelocity / 60 / getWidth() * (MAX_FREQUENCY - MIN_FREQUENCY));

            // 应用减速效果（每帧速度衰减）
            lastVelocity *= DECELERATION_RATE;

            // 16ms后执行下一帧（约60fps）
            postDelayed(this, 16);
        }
    };


    /**
     * 根据变化量调整频率值
     * @param deltaFrequency 频率变化量（可正可负）
     */
    private void adjustFrequencyByDelta(float deltaFrequency) {
        // 计算新频率值
        float newFrequency = currentFrequency + deltaFrequency;

        // 确保频率在合法范围内（MIN_FREQUENCY ~ MAX_FREQUENCY）
        newFrequency = Math.max(MIN_FREQUENCY, Math.min(MAX_FREQUENCY, newFrequency));

        // 更新频率并重绘视图
        setCurrentFrequency(newFrequency);
    }

    public void setCurrentFrequency(float frequency) {
        this.currentFrequency = Math.max(MIN_FREQUENCY, Math.min(MAX_FREQUENCY, frequency));
        invalidate();
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int spToPx(float sp) {
        return (int) (sp * getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }
}