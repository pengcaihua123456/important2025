package com.evenbus.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.modulebufferknifeantotations.BindView;
import com.example.modulebufferknifeantotations.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public TextView tv_trace;
    public TextView tv_memory;
    public TextView tv_asm;
    public TextView tv_view;

    public TextView tv_arount;

    @BindView(R.id.tv_compler)
    public TextView tv_compler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @OnClick(R.id.tv_compler)
    public void onclick(View v){

    }

    private void initView() {
        tv_arount=findViewById(R.id.tv_arount);
        tv_trace = findViewById(R.id.tv_trace);
        tv_memory = findViewById(R.id.tv_memory);
        tv_asm = findViewById(R.id.tv_asm);
        tv_view = findViewById(R.id.tv_view);
        tv_trace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trace();
            }
        });
        tv_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMy();
            }
        });

        tv_memory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Memory();
            }
        });

        tv_arount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过路由直接打开home组件的HomeActivity，
                arounter();
            }
        });
    }

    private void arounter(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, ArounterActivity.class);
        startActivity(intent);
    }


    private void Memory(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, OomActivity.class);
        startActivity(intent);
    }

    private void trace(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, MatrixActivity.class);
        startActivity(intent);
    }

    private void viewMy(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(MainActivity.this, ViewActivity.class);
        startActivity(intent);
    }


}
