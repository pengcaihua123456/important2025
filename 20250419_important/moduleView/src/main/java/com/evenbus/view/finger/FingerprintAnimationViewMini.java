package com.evenbus.view.finger;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/****
 * 类似点赞效果
 */
public class FingerprintAnimationViewMini extends View {
    private static final int MAX_CIRCLES = 15;
    private static final int CIRCLE_LIFETIME = 1000; // ms
    private static final int CIRCLE_SPAWN_RATE = 100; // ms

    private List<Circle> circles = new ArrayList<>();
    private Paint paint;
    private Random random = new Random();
    private long lastSpawnTime = 0;
    private PointF touchPoint = null;

    public FingerprintAnimationViewMini(Context context) {
        super(context);
        init();
    }

    public FingerprintAnimationViewMini(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long currentTime = System.currentTimeMillis();

        // Spawn new circles if finger is down
        if (touchPoint != null && currentTime - lastSpawnTime > CIRCLE_SPAWN_RATE) {
            spawnCircles();
            lastSpawnTime = currentTime;
        }

        // Draw and update circles
        Iterator<Circle> iterator = circles.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();

            // Update position
            circle.y -= circle.speed;

            // Update alpha based on lifetime
            float progress = (currentTime - circle.birthTime) / (float)CIRCLE_LIFETIME;
            if (progress >= 1) {
                iterator.remove();
                continue;
            }

            // Draw the circle
            paint.setColor(Color.argb((int)(255 * (1 - progress)), 255, 0, 0));
            canvas.drawCircle(circle.x, circle.y, circle.radius, paint);
        }

        // Redraw if there are circles to animate
        if (!circles.isEmpty()) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchPoint = new PointF(event.getX(), event.getY());
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchPoint = null;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void spawnCircles() {
        if (touchPoint == null || circles.size() >= MAX_CIRCLES) {
            return;
        }

        int circleCount = random.nextInt(3) + 1; // 1-3 circles at a time

        for (int i = 0; i < circleCount; i++) {
            Circle circle = new Circle();
            circle.birthTime = System.currentTimeMillis();

            // Random position around touch point
            float angle = random.nextFloat() * 2 * (float)Math.PI;
            float distance = random.nextFloat() * 50;
            circle.x = touchPoint.x + (float)Math.cos(angle) * distance;
            circle.y = touchPoint.y + (float)Math.sin(angle) * distance;

            // Random properties
            circle.radius = random.nextFloat() * 10 + 5; // 5-15px
            circle.speed = random.nextFloat() * 3 + 1; // 1-4px/frame

            circles.add(circle);
        }
    }

    private static class Circle {
        float x;
        float y;
        float radius;
        float speed;
        long birthTime;
    }
}