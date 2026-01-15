package com.evenbus.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.trace.LaggyAnimationActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        textView = findViewById(R.id.tv_trace);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                traceAnim();
            }
        });
    }

    private void traceAnim(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, LaggyAnimationActivity.class);
        startActivity(intent);
    }

    private void trace(){
        // 在按钮点击或其他事件中
//        Intent intent = new Intent(MainActivity.this, TraceActivity.class);
//        startActivity(intent);
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
