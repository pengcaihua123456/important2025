package com.evenbus.view.radio;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
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

public class RadioFrequencyMiniView extends View {

    private static final String TAG="RadioFrequencyPlusView";
    // 频率范围常量
    private static final float MIN_FREQUENCY = 87.5f;  // 最小频率值(MHz)
    private static final float MAX_FREQUENCY = 108.0f; // 最大频率值(MHz)

    // 视图状态变量
    private float currentFrequency = 92.0f; // 当前频率值，默认为92.0MHz

    // 绘制工具
    private Paint linePaint;      // 用于绘制刻度线的画笔
    private Paint textPaint;      // 用于绘制文字的画笔
    private Paint indicatorPaint; // 用于绘制指示器的画笔

    // 视图尺寸参数
    private int padding = 20;     // 视图内边距(像素)
    /***
     * 为什么是2倍，可以滑到最左和最右进行分析！
     */
    private int scaleLengthMultiplier = 2; // 刻度尺长度相对于视图宽度的倍数

    // 刻度参数
    private float majorStep = 1.0f;   // 主刻度间隔(1MHz)
    private float minorStep = 0.1f;   // 次刻度间隔(0.1MHz)
    private int majorLineLength = 30; // 主刻度线长度(dp)
    private int minorLineLength = 16; // 次刻度线长度(dp)
    private int indicatorLength = 40; // 指示器长度(dp)

    /**
     * 构造方法1：通过代码创建视图时调用
     */
    public RadioFrequencyMiniView(Context context) {
        this(context, null);
    }

    /**
     * 构造方法2：通过XML布局创建视图时调用
     */
    public RadioFrequencyMiniView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法3：带有默认样式属性的构造方法
     */
    public RadioFrequencyMiniView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化视图
        init();
    }

    /**
     * 初始化方法：设置画笔参数等初始化工作
     */
    private void init() {
        // 初始化刻度线画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG); // 开启抗锯齿
        linePaint.setColor(Color.BLACK);             // 设置颜色为黑色
        linePaint.setStrokeWidth(2);                 // 设置线宽2像素

        // 初始化文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(spToPx(12));           // 设置文字大小为12sp
        textPaint.setTextAlign(Paint.Align.CENTER);   // 设置文字居中对齐

        // 初始化指示器画笔
        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setColor(Color.RED);          // 设置颜色为红色
        indicatorPaint.setStrokeWidth(4);            // 设置线宽4像素
    }

    /**
     * 测量方法：确定视图的尺寸
     * @param widthMeasureSpec  宽度测量规格
     * @param heightMeasureSpec 高度测量规格
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取宽度测量模式和尺寸
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        // 计算默认高度（80dp转换为像素）
        int height = dpToPx(80);

        // 处理高度测量模式
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            // 如果是AT_MOST模式（对应wrap_content），取计算高度和测量高度的较小值
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            // 如果是EXACTLY模式（对应固定值或match_parent），直接使用测量高度
            height = MeasureSpec.getSize(heightMeasureSpec);
        }

        // 设置最终测量尺寸
        setMeasuredDimension(widthSize, height);
    }

    /**
     * 绘制方法：执行实际的绘制操作
     * @param canvas 画布对象，用于绘制各种图形和文字
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 获取视图尺寸
        int width = getWidth();
        int height = getHeight();

        // 计算视图中心线的Y坐标
        float centerY = height / 2f;

        // 计算刻度尺的总长度（比可见区域长，以便指示器可以居中）
        // 公式：刻度尺长度 = (视图宽度 - 两边内边距) * 倍数
        float scaleLength = (width - 2 * padding) * scaleLengthMultiplier;

        // 计算当前频率在刻度尺上的位置（0到scaleLength之间的值）
        float currentPos = (currentFrequency - MIN_FREQUENCY) / (MAX_FREQUENCY - MIN_FREQUENCY) * scaleLength;


        // 计算刻度尺的绘制起点，使当前频率位于视图中央
        // 公式：起点X = 视图中心X - 当前频率位置
        float startX = width / 2f - currentPos;
        startX =0f;
        Log.d(TAG,"startX:"+startX);

        // 绘制主刻度线（每1MHz一个主刻度）
        int majorCount = (int)((MAX_FREQUENCY - MIN_FREQUENCY) / majorStep) + 1;

        Log.d(TAG,"scaleLength:"+scaleLength+" ,currentPos:"+currentPos+" ,startX:"+startX+", majorCount: "+majorCount);


        for (int i = 0; i < majorCount; i++) {
            // 计算当前主刻度对应的频率值
            float frequency = MIN_FREQUENCY + i * majorStep;

            // 计算当前主刻度在画布上的X坐标
            float x = startX + (frequency - MIN_FREQUENCY) / (MAX_FREQUENCY - MIN_FREQUENCY) * scaleLength;

            Log.d(TAG,"frequency:"+frequency+" ,x:"+x);

            // 只绘制位于可见区域内的刻度（避免不必要的绘制）
            if (x >= padding && x <= width - padding) {
                // 绘制主刻度线（垂直线）
                canvas.drawLine(x, centerY - dpToPx(majorLineLength/2),
                        x, centerY + dpToPx(majorLineLength/2), linePaint);

                // 绘制主刻度值（MHz整数部分）
                String text = String.format("%.0f", frequency);
                canvas.drawText(text, x, centerY + dpToPx(majorLineLength/2 + 20), textPaint);
            }
        }

        // 绘制次刻度线（每0.1MHz一个次刻度）
        int minorCount = (int)((MAX_FREQUENCY - MIN_FREQUENCY) / minorStep) + 1;
        for (int i = 0; i < minorCount; i++) {
            // 计算当前次刻度对应的频率值
            float frequency = MIN_FREQUENCY + i * minorStep;

            // 计算当前次刻度在画布上的X坐标
            float x = startX + (frequency - MIN_FREQUENCY) / (MAX_FREQUENCY - MIN_FREQUENCY) * scaleLength;

            // 只绘制位于可见区域内的刻度
            if (x >= padding && x <= width - padding) {
                // 如果是主刻度点则跳过（避免重复绘制）
                if (i % 10 == 0) continue;

                // 绘制次刻度线（比主刻度线短）
                canvas.drawLine(x, centerY - dpToPx(minorLineLength/2),
                        x, centerY + dpToPx(minorLineLength/2), linePaint);
            }
        }

        // 绘制当前频率指示器（始终位于视图中央）
        float indicatorX = width / 2f;
        canvas.drawLine(indicatorX, centerY - dpToPx(indicatorLength/2),
                indicatorX, centerY + dpToPx(indicatorLength/2), indicatorPaint);

        // 在指示器上方绘制当前频率值（带1位小数）
        String currentText = String.format("%.1f MHz", currentFrequency);
        canvas.drawText(currentText, indicatorX, centerY - dpToPx(indicatorLength/2 + 10), textPaint);
    }

    /**
     * 设置当前频率并刷新视图
     * @param frequency 要设置的频率值(MHz)，范围必须在87.5到108.0之间
     */
    public void setCurrentFrequency(float frequency) {
        // 检查频率范围是否合法
        if (frequency < MIN_FREQUENCY) {
            frequency = MIN_FREQUENCY;
        } else if (frequency > MAX_FREQUENCY) {
            frequency = MAX_FREQUENCY;
        }

        // 更新当前频率
        this.currentFrequency = frequency;

        // 请求重绘视图
        invalidate();
    }

    /**
     * 工具方法：将dp值转换为px值
     * @param dp 要转换的dp值
     * @return 对应的像素值
     */
    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 工具方法：将sp值转换为px值
     * @param sp 要转换的sp值
     * @return 对应的像素值
     */
    private int spToPx(float sp) {
        return (int) (sp * getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }
}