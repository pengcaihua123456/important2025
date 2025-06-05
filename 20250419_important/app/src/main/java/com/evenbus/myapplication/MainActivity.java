package com.evenbus.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.leak.LeakThreadActivity;
import com.evenbus.myapplication.leak.oom.OomRecyclerActivity;
import com.evenbus.myapplication.leak.videoleak.VideoPlayerActivity;
import com.evenbus.myapplication.trace.LaggyAnimationActivity;
import com.evenbus.myapplication.trace.TraceActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public TextView tv_trace;
    public TextView tv_memory;
    public TextView tv_asm;
    public TextView tv_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        tv_trace = findViewById(R.id.tv_trace);
        tv_memory = findViewById(R.id.tv_memory);
        tv_asm = findViewById(R.id.tv_asm);
        tv_view = findViewById(R.id.tv_view);
        tv_trace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                traceAnim();
                ViewMemoryLeak();
            }
        });
        tv_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMy();
            }
        });

    }

    private void traceAnim(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, LaggyAnimationActivity.class);
        startActivity(intent);
    }


    private void MemoryImage(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, OomRecyclerActivity.class);
        startActivity(intent);
    }

    private void MemoryLeak() {
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, LeakThreadActivity.class);
        startActivity(intent);
    }

    private void ViewMemoryLeak() {
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
        startActivity(intent);
    }

    private void trace(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, TraceActivity.class);
        startActivity(intent);
    }

    private void viewMy(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, ViewActivity.class);
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
