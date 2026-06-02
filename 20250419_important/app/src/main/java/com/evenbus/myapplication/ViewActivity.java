package com.evenbus.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.view.charge.AnimVivoActivity;
import com.evenbus.view.circle.CircleChargeActivity;
import com.evenbus.view.extand.ExpandActivity;
import com.evenbus.view.flow.FlowViewActivity;
import com.evenbus.view.light.BorderLightActivity;
import com.evenbus.view.light.GradientBorderActivity;
import com.evenbus.view.radio.RadioFrequencyUltraActivity;
import com.evenbus.view.wheel.WheelActivity;
import com.evenbus.view.doubao.ShimmerCardActivity;
import com.evenbus.view.deepseek.AnimatedBorderCardActivity;
import com.evenbus.view.qianwen.RoundBorderShimmerActivity;

public class ViewActivity extends AppCompatActivity {

    public TextView tv_trace;
    public TextView tv_memory;
    public TextView tv_asm;
    public TextView tv_view;
    public TextView tv_expand;

    public TextView tv_circle;
    public TextView tv_light;
    public TextView tv_gradient_border;
    public TextView tv_shimmer_card;
    public TextView tv_animated_border_card;
    public TextView tv_round_border_shimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);
        tv_trace = findViewById(R.id.tv_trace);
        tv_memory = findViewById(R.id.tv_memory);
        tv_asm = findViewById(R.id.tv_asm);
        tv_view = findViewById(R.id.tv_view);
        tv_expand = findViewById(R.id.tv_expand);
        tv_circle = findViewById(R.id.tv_circle);
        tv_light = findViewById(R.id.tv_light);
        tv_trace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioActivity();
            }
        });
        tv_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VivoActivity();
            }
        });

        tv_asm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlowActivity();
            }
        });

        tv_memory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wheelActivity();
            }
        });

        tv_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpandActivity();
            }
        });

        tv_circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleActivity();
            }
        });

        tv_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lightActivity();
            }
        });

        tv_gradient_border = findViewById(R.id.tv_gradient_border);
        tv_gradient_border.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientBorderActivity();
            }
        });

        tv_shimmer_card = findViewById(R.id.tv_shimmer_card);
        tv_shimmer_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shimmerCardActivity();
            }
        });

        tv_animated_border_card = findViewById(R.id.tv_animated_border_card);
        tv_animated_border_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animatedBorderCardActivity();
            }
        });

        tv_round_border_shimmer = findViewById(R.id.tv_round_border_shimmer);
        tv_round_border_shimmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roundBorderShimmerActivity();
            }
        });
    }

    private void circleActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, CircleChargeActivity.class);
        startActivity(intent);
    }

    private void ExpandActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, ExpandActivity.class);
        startActivity(intent);
    }

    private void VivoActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, AnimVivoActivity.class);
        startActivity(intent);
    }

    private void FlowActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, FlowViewActivity.class);
        startActivity(intent);
    }

    private void wheelActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, WheelActivity.class);
        startActivity(intent);
    }
    private void RadioActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, RadioFrequencyUltraActivity.class);
        startActivity(intent);
    }

    private void lightActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, BorderLightActivity.class);
        startActivity(intent);
    }

    private void gradientBorderActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, GradientBorderActivity.class);
        startActivity(intent);
    }

    private void shimmerCardActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, ShimmerCardActivity.class);
        startActivity(intent);
    }

    private void animatedBorderCardActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, AnimatedBorderCardActivity.class);
        startActivity(intent);
    }

    private void roundBorderShimmerActivity(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(ViewActivity.this, RoundBorderShimmerActivity.class);
        startActivity(intent);
    }

}
