package com.evenbus.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.trace.LaggyAnimationActivity;
import com.evenbus.myapplication.trace.LaggyAnimationActivity2;
import com.evenbus.myapplication.trace.TraceRecycleActivity;

public class MatrixActivity extends AppCompatActivity {

    private static final String TAG = "MatrixActivity";
    public TextView tv_trace;
    public TextView tv_memory;
    public TextView tv_asm;
    public TextView tv_view;

    public TextView tv_arount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        tv_arount=findViewById(R.id.tv_arount);
        tv_trace = findViewById(R.id.tv_trace);
        tv_memory = findViewById(R.id.tv_memory);
        tv_asm = findViewById(R.id.tv_asm);
        tv_view = findViewById(R.id.tv_view);

        tv_trace.setText("动画卡顿");
        tv_view.setText("复杂动画卡顿");
        tv_arount.setText("recycleView卡顿");
        tv_asm.setText("ANR监控");
        tv_trace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                traceAnim();
            }
        });
        tv_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Anim2();

            }
        });

        tv_arount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                traceRecycle();
            }
        });
        tv_asm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testThreadAnr();
            }
        });

    }

    private void traceAnim(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MatrixActivity.this, LaggyAnimationActivity.class);
        startActivity(intent);
    }


    private void Anim2(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MatrixActivity.this, LaggyAnimationActivity2.class);
        startActivity(intent);
    }

    private void traceRecycle(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MatrixActivity.this, TraceRecycleActivity.class);
        startActivity(intent);
    }

    private void testThreadAnr() {
        try {
            int number = 0;
            while (number++ < 5) {
                Log.e(TAG, "主线程睡眠导致的ANR:次数" + number + "/5");
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, "异常信息为:" + e.getMessage());
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "异常信息为:" + e.getMessage());
        }
    }
}
