package com.evenbus.view.shadow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.util.AttributeSet;
import android.view.View;

public class RadialGradientDemoView extends View {
    
    private Paint paint;
    private RadialGradient radialGradient;
    private int width, height;
    
    public RadialGradientDemoView(Context context) {
        this(context, null);
    }
    
    public RadialGradientDemoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public RadialGradientDemoView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        
        // 创建 RadialGradient
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(width, height) / 3f;
        
        int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
        float[] positions = {0f, 0.25f, 0.5f, 0.75f, 1f};
        
        radialGradient = new RadialGradient(centerX, centerY, radius, colors, positions, android.graphics.Shader.TileMode.REPEAT);
        paint.setShader(radialGradient);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制背景
        canvas.drawColor(Color.BLACK);
        
        // 绘制 RadialGradient 圆形
        if (radialGradient != null) {
            canvas.drawCircle(width / 2f, height / 2f, Math.min(width, height) / 3f, paint);
        }
        
        // 绘制说明文字
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        String text = "RadialGradient (径向渐变)";
        float textWidth = textPaint.measureText(text);
        canvas.drawText(text, (width - textWidth) / 2, height - 40, textPaint);
    }
}