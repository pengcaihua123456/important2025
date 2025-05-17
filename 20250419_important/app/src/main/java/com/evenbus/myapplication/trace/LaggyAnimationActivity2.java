package com.evenbus.myapplication.trace;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class LaggyAnimationActivity2 extends AppCompatActivity {

    private View animationView;
    private List<View> views = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laggy_animation);

        animationView = findViewById(R.id.animation_view);

        // 创建100个动画视图
        ViewGroup container = findViewById(R.id.container);
        for (int i = 0; i < 10000; i++) {
            View view = new View(this);
            view.setLayoutParams(new ViewGroup.LayoutParams(20, 20));
            view.setBackgroundColor(Color.RED);
            container.addView(view);
            views.add(view);
        }

        // 启动复杂动画
        startLaggyAnimation();
    }

    private void startLaggyAnimation() {
        // 复杂路径动画
        Path path = new Path();
        path.moveTo(0, 0);
        for (int i = 1; i <= 50; i++) {
            path.lineTo(i * 20, (float) (Math.random() * 500));
        }

        final PathMeasure pathMeasure = new PathMeasure(path, false);
        final float pathLength = pathMeasure.getLength();

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(5000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                float distance = fraction * pathLength;
                float[] pos = new float[2];
                pathMeasure.getPosTan(distance, pos, null);
                animationView.setTranslationX(pos[0]);
                animationView.setTranslationY(pos[1]);

                // 同时更新100个子视图的动画 - 导致卡顿
                for (int i = 0; i < views.size(); i++) {
                    View view = views.get(i);
                    view.setTranslationX(pos[0] + i * 5);
                    view.setTranslationY(pos[1] + i * 5);
                    view.setRotation(fraction * 360 * 5);
                    view.setScaleX(0.5f + fraction);
                    view.setScaleY(0.5f + fraction);
                }
            }
        });
        animator.start();
    }
}