package com.evenbus.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.view.charge.AnimVivoActivity;
import com.evenbus.myapplication.view.extand.ExpandActivity;
import com.evenbus.myapplication.view.flow.FlowViewActivity;
import com.evenbus.myapplication.view.radio.RadioFrequencyUltraActivity;
import com.evenbus.myapplication.view.wheel.WheelActivity;

public class ViewActivity extends AppCompatActivity {

    public TextView tv_trace;
    public TextView tv_memory;
    public TextView tv_asm;
    public TextView tv_view;
    public TextView tv_expand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);
        tv_trace = findViewById(R.id.tv_trace);
        tv_memory = findViewById(R.id.tv_memory);
        tv_asm = findViewById(R.id.tv_asm);
        tv_view = findViewById(R.id.tv_view);
        tv_expand = findViewById(R.id.tv_expand);
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


}
