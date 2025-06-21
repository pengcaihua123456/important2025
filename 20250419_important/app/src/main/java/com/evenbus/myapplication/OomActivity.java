package com.evenbus.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.leak.LeakThreadActivity;
import com.evenbus.myapplication.leak.oom.OomDestoryImageActivity;
import com.evenbus.myapplication.leak.oom.OomOriginImageActivity;
import com.evenbus.myapplication.leak.oom.OomQueueImageActivity;
import com.evenbus.myapplication.leak.oom.OomRecyclerActivity;
import com.evenbus.myapplication.leak.videoleak.CoverVideoPlayerView;
import com.example.modulebufferknifeantotations.BindView;
import com.example.modulebufferknifeantotations.OnClick;

public class OomActivity extends AppCompatActivity {

    private static final String TAG = "OomActivity";
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
        coverVidePlay();
    }

    private void initView() {
        tv_arount=findViewById(R.id.tv_arount);
        tv_trace = findViewById(R.id.tv_trace);
        tv_memory = findViewById(R.id.tv_memory);
        tv_asm = findViewById(R.id.tv_asm);
        tv_view = findViewById(R.id.tv_view);
        tv_compler = findViewById(R.id.tv_compler);


        tv_arount.setText("线程泄露");
        tv_trace.setText("没有销毁");
        tv_memory.setText("没有压缩");
        tv_asm.setText("存储太大OOM");
        tv_view.setText("Recycle OOM");
        tv_compler.setText("内存泄露");


        tv_trace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destory();
            }
        });
        tv_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recycleImage();
            }
        });

        tv_arount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadLeak();
            }
        });

        tv_memory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                originImage();
            }
        });


        tv_asm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queueImage();
            }
        });

    }

    private void destory(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(OomActivity.this, OomDestoryImageActivity.class);
        startActivity(intent);
    }


    private void originImage(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(OomActivity.this, OomOriginImageActivity.class);
        startActivity(intent);
    }

    private void queueImage() {
        // 在按钮点击或其他事件中
        Intent intent = new Intent(OomActivity.this, OomQueueImageActivity.class);
        startActivity(intent);
    }

    private void recycleImage() {
        // 在按钮点击或其他事件中
        Intent intent = new Intent(OomActivity.this, OomRecyclerActivity.class);
        startActivity(intent);
    }

    private void coverVidePlay(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(OomActivity.this, CoverVideoPlayerView.class);
        startActivity(intent);
    }

    private void threadLeak(){
        // 在按钮点击或其他事件中
        Intent intent = new Intent(OomActivity.this, LeakThreadActivity.class);
        startActivity(intent);
    }

}
