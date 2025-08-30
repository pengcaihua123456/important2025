package com.evenbus.myapplication.trace;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class LaggyAnimationOtherActivity extends AppCompatActivity {

    private static final int ITEM_COUNT = 100; // 使用更多视图
    private List<View> animatedViews = new ArrayList<>();
    private boolean isRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laggy_animation_per);

        ViewGroup container = (ViewGroup) findViewById(R.id.animation_container);

        // 添加大量复杂视图
        for (int i = 0; i < ITEM_COUNT; i++) {
            // 使用自定义View增加绘制复杂度
            ComplexView view = new ComplexView(this);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(100, 100);
            view.setLayoutParams(params);
            container.addView(view);
            animatedViews.add(view);
        }

        startExtremeAnimations();

    }

    private void startExtremeAnimations() {
        // 为每个视图创建多个复杂动画
        for (int i = 0; i < animatedViews.size(); i++) {
            final View view = animatedViews.get(i);

            // 1. 复杂路径动画
            final Path path = createComplexPath(i);
            final PathMeasure pathMeasure = new PathMeasure(path, false);
            final float pathLength = pathMeasure.getLength();

            // 2. 使用ValueAnimator进行实时计算
            ValueAnimator pathAnimator = ValueAnimator.ofFloat(0, 1);
            pathAnimator.setDuration(5000);
            pathAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (!isRunning) return;

                    float fraction = (float) animation.getAnimatedValue();
                    float distance = fraction * pathLength;
                    float[] pos = new float[2];
                    pathMeasure.getPosTan(distance, pos, null);

                    // 实时计算并设置多个属性
                    view.setTranslationX(pos[0]);
                    view.setTranslationY(pos[1]);
                    view.setRotation(fraction * 360 * 5);
                    view.setScaleX(0.5f + (float) Math.sin(fraction * Math.PI * 4));
                    view.setScaleY(0.5f + (float) Math.cos(fraction * Math.PI * 4));

                    // 强制重绘增加负担
                    view.invalidate();
                }
            });
            pathAnimator.start();

            // 3. 额外添加颜色变化动画
            ValueAnimator colorAnimator = ValueAnimator.ofArgb(Color.RED, Color.BLUE);
            colorAnimator.setDuration(3000);
            colorAnimator.setRepeatCount(ValueAnimator.INFINITE);
            colorAnimator.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (view instanceof ComplexView) {
                        ((ComplexView) view).setCustomColor((int) animation.getAnimatedValue());
                    }
                }
            });
            colorAnimator.start();
        }
    }

    private Path createComplexPath(int index) {
        Path path = new Path();
        path.moveTo(0, index * 10);
        for (int i = 1; i <= 100; i++) { // 非常复杂的路径
            float x = i * 15;
            float y = (float) (Math.sin(i * 0.2 + index * 0.1) * 200 + index * 10);
            path.lineTo(x, y);
        }
        return path;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    // 自定义复杂View增加绘制负担
    static class ComplexView extends View {
        private int customColor = Color.RED;
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public ComplexView(Context context) {
            super(context);
            paint.setStyle(Paint.Style.FILL);
        }

        public void setCustomColor(int color) {
            this.customColor = color;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // 复杂绘制操作
            paint.setColor(customColor);
            canvas.drawCircle(getWidth()/2f, getHeight()/2f, getWidth()/2f, paint);

            // 刻意增加绘制复杂度
            paint.setColor(Color.WHITE);
            for (int i = 0; i < 800; i++) {
                float angle = (float) (i * Math.PI * 2 / 10);
                float x = (float) (getWidth()/2f + Math.cos(angle) * getWidth()/3f);
                float y = (float) (getHeight()/2f + Math.sin(angle) * getHeight()/3f);
                canvas.drawCircle(x, y, getWidth()/10f, paint);
            }
        }
    }
}