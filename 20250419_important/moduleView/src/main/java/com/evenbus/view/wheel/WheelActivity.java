package com.evenbus.view.wheel;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_view.R;


public class WheelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel_view);
        setTitle("自定义3D滚轮，时间滚轮");
    }
}
