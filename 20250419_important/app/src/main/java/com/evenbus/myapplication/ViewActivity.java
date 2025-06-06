package com.evenbus.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.view.flow.FlowViewActivity;
import com.evenbus.myapplication.view.radio.RadioFrequencyUltraActivity;
import com.evenbus.myapplication.view.wheel.WheelActivity;

public class ViewActivity extends AppCompatActivity {

    public TextView tv_trace;
    public TextView tv_memory;
    public TextView tv_asm;
    public TextView tv_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);
        tv_trace = findViewById(R.id.tv_trace);
        tv_memory = findViewById(R.id.tv_memory);
        tv_asm = findViewById(R.id.tv_asm);
        tv_view = findViewById(R.id.tv_view);
        tv_trace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioActivity();
            }
        });
        tv_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wheelActivity();
            }
        });

        tv_asm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlowActivity();
            }
        });

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


}
