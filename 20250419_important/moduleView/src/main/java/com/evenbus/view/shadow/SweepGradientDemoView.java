package com.evenbus.view.shadow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

public class SweepGradientDemoView extends View {
    
    private Paint paint;
    private SweepGradient sweepGradient;
    private Matrix matrix;
    private int width, height;
    private float rotationAngle = 0f;
    
    public SweepGradientDemoView(Context context) {
        this(context, null);
    }
    
    public SweepGradientDemoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public SweepGradientDemoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matrix = new Matrix();
        paint.setStyle(Paint.Style.STROKE);// 设置为实心模式
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        
        // 创建 SweepGradient
        float centerX = width / 2f;
        float centerY = height / 2f;
        
        int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.RED};
        float[] positions = {0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f};
        
        sweepGradient = new SweepGradient(centerX, centerY, colors, positions);
        paint.setShader(sweepGradient);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制背景
        canvas.drawColor(Color.BLACK);
        
        // 更新旋转角度
        rotationAngle += 2f;
        if (rotationAngle >= 360f) {
            rotationAngle = 0f;
        }
        
        // 应用旋转矩阵
        matrix.reset();
        matrix.postRotate(rotationAngle, width / 2f, height / 2f);
        sweepGradient.setLocalMatrix(matrix);
        
        // 绘制 SweepGradient 长方形
        if (sweepGradient != null) {
            float rectWidth = width * 0.6f;
            float rectHeight = height * 0.6f;
            float left = (width - rectWidth) / 2;
            float top = (height - rectHeight) / 2;
            float right = left + rectWidth;
            float bottom = top + rectHeight;
            canvas.drawRect(left, top, right, bottom, paint);
        }
        
        // 绘制说明文字
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        String text = "SweepGradient (扫描渐变)";
        float textWidth = textPaint.measureText(text);
        canvas.drawText(text, (width - textWidth) / 2, height - 40, textPaint);
        
        // 触发重绘以产生动画
        postInvalidateDelayed(16);
    }
}