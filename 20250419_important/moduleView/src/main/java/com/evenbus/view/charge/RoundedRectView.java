package com.evenbus.view.charge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class RoundedRectView extends View {

    String TAG ="RoundedRectView";


    private Paint paint;
    private float cornerRadius = 40f; // 初始圆角半径
    private int color = Color.parseColor("#00FFCC"); // 青色

    // 动画相关属性
    private float scaleX = 1f;
    private float scaleY = 1f;
    private float alpha = 1f;

    public RoundedRectView(Context context) {
        super(context);
        init();
    }

    public RoundedRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    // 设置圆角半径
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }

    public float getCornerRadius() {
        return cornerRadius;
    }

    // 设置缩放比例
    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
        invalidate();
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
        invalidate();
    }

    // 设置透明度
    public void setViewAlpha(float alpha) {
        this.alpha = alpha;
        setAlpha(alpha);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 计算缩放后的尺寸
        float scaledWidth = getWidth() * scaleX;
        float scaledHeight = getHeight() * scaleY;

        // 计算绘制位置(居中)
        float left = (getWidth() - scaledWidth) / 2;
        float top = (getHeight() - scaledHeight) / 2;
        float right = left + scaledWidth;
        float bottom = top + scaledHeight;

        // 计算实际使用的圆角半径（不能超过短边的一半）
        float usedRadius = Math.min(cornerRadius, Math.min(scaledWidth, scaledHeight) / 2);
        Log.d(TAG,"usedRadius"+usedRadius);

        usedRadius=usedRadius*3;

        // 绘制形状
        Path path = new Path();
        RectF rect = new RectF(left, top, right, bottom);
        path.addRoundRect(rect, usedRadius, usedRadius, Path.Direction.CW);

        paint.setAlpha((int)(alpha * 255));
        canvas.drawPath(path, paint);
    }
}