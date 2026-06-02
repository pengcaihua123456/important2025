package com.evenbus.view.light;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.Intent;

import com.evenbus.view.shadow.GradientDemoActivity;
import com.example.module_view.R;

public class BorderLightActivity extends Activity {

    private BorderLightView_1 borderLightView;
    private SeekBar arcWidthSeekBar;
    private SeekBar speedSeekBar;
    private SeekBar blurWidthSeekBar;
    private TextView arcWidthText;
    private TextView speedText;
    private TextView blurWidthText;
    private Button toggleButton;
    private Button gradientDemoButton;

    private boolean isAnimating = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_border_light);

        borderLightView = findViewById(R.id.border_light_view);
        arcWidthSeekBar = findViewById(R.id.seekbar_arc_width);
        speedSeekBar = findViewById(R.id.seekbar_speed);
        blurWidthSeekBar = findViewById(R.id.seekbar_blur_width);
        arcWidthText = findViewById(R.id.text_arc_width);
        speedText = findViewById(R.id.text_speed);
        blurWidthText = findViewById(R.id.text_blur_width);
        toggleButton = findViewById(R.id.btn_toggle);

        // 初始值
        arcWidthSeekBar.setProgress(60); // 对应60度
        speedSeekBar.setProgress(30); // 对应3秒
        blurWidthSeekBar.setProgress(12); // 对应12f模糊半径

        updateArcWidthText(60);
        updateSpeedText(30, 3650);
        updateBlurWidthText(12);

        arcWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float degrees = progress;
                borderLightView.setLightArcWidth(degrees);
                updateArcWidthText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 进度 1-100，映射到持续时间 5000ms - 500ms
                int duration = 5000 - (progress * 45); // 5000 - 45*100 = 500
                borderLightView.setAnimationDuration(duration);
                updateSpeedText(progress, duration);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        blurWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 进度 0-50，映射到模糊半径 1f-50f
                float radius = Math.max(1f, progress); // 确保最小为1
                borderLightView.setBlurRadius(radius);
                updateBlurWidthText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnimating) {
                    borderLightView.stopAnimation();
                    toggleButton.setText("开始动画");
                } else {
                    borderLightView.startAnimation();
                    toggleButton.setText("停止动画");
                }
                isAnimating = !isAnimating;
            }
        });

        // 渐变效果演示按钮
        gradientDemoButton = findViewById(R.id.btn_gradient_demo);
        gradientDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BorderLightActivity.this, GradientDemoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateArcWidthText(int degrees) {
        arcWidthText.setText("光弧宽度: " + degrees + "°");
    }

    private void updateSpeedText(int progress, int duration) {
        // 进度 1-100，映射到速度 慢-快
        speedText.setText("旋转速度: " + progress + " (时长: " + duration + "ms)");
    }

    private void updateBlurWidthText(int radius) {
        blurWidthText.setText("模糊宽度: " + radius);
    }
}