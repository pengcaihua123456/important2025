package com.evenbus.view.shadow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LinearGradientDemoView extends View {
    
    private Paint paint;
    private LinearGradient linearGradient;
    private int width, height;
    private float offset = 0f;
    
    public LinearGradientDemoView(Context context) {
        this(context, null);
    }
    
    public LinearGradientDemoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public LinearGradientDemoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        updateGradient();
    }
    
    private void updateGradient() {
        // 创建 LinearGradient，从左到右
        int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
        float[] positions = {0f, 0.25f, 0.5f, 0.75f, 1f};
        
        // 添加偏移量，使渐变移动
        float startX = offset;
        float endX = width + offset;
        
        linearGradient = new LinearGradient(startX, height / 2f, endX, height / 2f, 
                colors, positions, android.graphics.Shader.TileMode.MIRROR);
        paint.setShader(linearGradient);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制背景
        canvas.drawColor(Color.BLACK);
        
        // 更新偏移量
        offset += 2f;
        if (offset >= width) {
            offset = 0f;
        }
        updateGradient();
        
        // 绘制 LinearGradient 矩形
        if (linearGradient != null) {
            float rectSize = Math.min(width, height) * 0.6f;
            float left = (width - rectSize) / 2;
            float top = (height - rectSize) / 2;
            float right = left + rectSize;
            float bottom = top + rectSize;
            
            canvas.drawRect(left, top, right, bottom, paint);
        }
        
        // 绘制说明文字
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        String text = "LinearGradient (线性渐变)";
        float textWidth = textPaint.measureText(text);
        canvas.drawText(text, (width - textWidth) / 2, height - 40, textPaint);
        
        // 触发重绘以产生动画
        postInvalidateDelayed(16);
    }
}