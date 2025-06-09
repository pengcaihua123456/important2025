package com.evenbus.myapplication.view.charge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.evenbus.myapplication.R;

public class AnimVivoActivity extends Activity {

    String TAG ="AnimVivoActivity";

    private RoundedRectView chargingRect;
    private TextView percentText;
    private TextView chargingText;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vivo_charge);

        // 初始化视图
        chargingRect = findViewById(R.id.charging_rect);
        percentText = findViewById(R.id.percent_text);
        percentText.setVisibility(View.GONE);
        chargingText = findViewById(R.id.charging_text);
        startButton = findViewById(R.id.start_button);

        chargingRect.setVisibility(View.GONE);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startExpandAnimation();
            }
        });

    }

    long time =200;

    private void startShrinkAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();

        // 1. 宽度收缩动画 - 从两边向中间收缩
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(
                chargingRect, "scaleX", 1f, 0.13f);
        scaleXAnim.setDuration(time);

        // 2. 高度收缩动画 - 从上下向中间收缩
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(
                chargingRect, "scaleY", 1f, 0.5f);
        scaleYAnim.setDuration(time);

        // 3. 圆角动画 - 变成完美圆形
        float finalRadius = Math.min(chargingRect.getWidth(), chargingRect.getHeight()) / 2f;
        Log.d(TAG, "finalRadius" + finalRadius);

        ObjectAnimator cornerAnim = ObjectAnimator.ofFloat(
                chargingRect, "cornerRadius", 40f, finalRadius);
        cornerAnim.setDuration(time);

        // 第一阶段动画：同时播放所有属性动画
        animatorSet.playTogether(scaleXAnim, scaleYAnim, cornerAnim);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        // 添加动画监听器，在第一阶段结束后执行第二阶段动画
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 第二阶段动画：圆形收缩
                startPerfectCircleShrinkAnimation();
            }
        });

        // 启动第一阶段动画
        animatorSet.start();
    }

    // 第二阶段动画：完美圆形收缩
    private void startPerfectCircleShrinkAnimation() {
        AnimatorSet shrinkAnimator = new AnimatorSet();

        // 1. 圆形继续收缩
        ObjectAnimator shrinkX = ObjectAnimator.ofFloat(
                chargingRect, "scaleX", 0.13f, 0f);
        shrinkX.setDuration(time);

        ObjectAnimator shrinkY = ObjectAnimator.ofFloat(
                chargingRect, "scaleY", 0.5f, 0f);
        shrinkY.setDuration(time);

        // 2. 透明度动画
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(
                chargingRect, "alpha", 1f, 0f);
        fadeOut.setDuration(time);

        shrinkAnimator.playTogether(shrinkX, shrinkY, fadeOut);
        shrinkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // 收缩完成后开始反向动画
        shrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startExpandAnimation(); // 开始反向扩展动画
            }
        });

        shrinkAnimator.start();
    }



    // 第三阶段：反向扩展动画
    private void startExpandAnimation() {

        // 先重置为完全透明和小尺寸
        chargingRect.setScaleX(0f);
        chargingRect.setScaleY(0f);
        chargingRect.setAlpha(0f);
        chargingRect.setVisibility(View.VISIBLE);

        AnimatorSet expandAnimator = new AnimatorSet();

        // 1. 圆形扩展
        ObjectAnimator expandX = ObjectAnimator.ofFloat(
                chargingRect, "scaleX", 0f, 0.13f);
        expandX.setDuration(time);

        ObjectAnimator expandY = ObjectAnimator.ofFloat(
                chargingRect, "scaleY", 0f, 0.5f);
        expandY.setDuration(time);

        // 2. 透明度恢复
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(
                chargingRect, "alpha", 0f, 1f);
        fadeIn.setDuration(time);

        expandAnimator.playTogether(expandX, expandY, fadeIn);
        expandAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // 扩展完成后恢复原始形状
        expandAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startRestoreAnimation(); // 开始恢复原始形状
            }
        });

        expandAnimator.start();
    }

    // 第四阶段：恢复原始形状
    private void startRestoreAnimation() {
        AnimatorSet restoreAnimator = new AnimatorSet();

        // 1. 恢复宽度
        ObjectAnimator restoreX = ObjectAnimator.ofFloat(
                chargingRect, "scaleX", 0.13f, 1f);
        restoreX.setDuration(time);

        // 2. 恢复高度
        ObjectAnimator restoreY = ObjectAnimator.ofFloat(
                chargingRect, "scaleY", 0.5f, 1f);
        restoreY.setDuration(time);

        // 3. 恢复圆角
        float finalRadius = Math.min(chargingRect.getWidth(), chargingRect.getHeight()) / 2f;
        ObjectAnimator cornerRestore = ObjectAnimator.ofFloat(
                chargingRect, "cornerRadius", finalRadius, 40f);
        cornerRestore.setDuration(time);
        restoreAnimator.playTogether(restoreX, restoreY, cornerRestore);
        restoreAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        restoreAnimator.start();

        restoreAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                startShrinkAnimation();
            }
        });
    }
}
