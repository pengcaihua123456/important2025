package com.example.matrixdemo;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.blankj.utilcode.util.LogUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends AppCompatActivity {
    public TextView textView;
    ImageView imageView;
    private static final String TAG = "MatrixLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        textView = findViewById(R.id.tv_test);
        imageView = findViewById(R.id.tv_ani);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "-----------4444444--" );

//                imageView.setBackgroundResource(R.drawable.animation_list);
//                AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
//                animationDrawable.start();

                testThreadAnr();

//                try {
//                    testThreadPool();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }


    private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    ExecutorService service;
    private void testThreadPool() throws InterruptedException {


//        service= new ThreadPoolExecutor(0, 6,
//                0L, TimeUnit.SECONDS,
//                new ArrayBlockingQueue<Runnable>(6));
//
//        service= new ThreadPoolExecutor(0, 6,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>());

        service= Executors.newFixedThreadPool(6);

        for(int i=0;i<1000;i++){
            final int index=i;
//            Log.e("peng","存放:"+index);
            Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    Log.e("peng","index:"+index + Thread.currentThread().getName());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            queue.put(runnable);
        }

        while (true){
            try {
                Runnable runnable=queue.take();
                if(runnable!=null){
                    service.execute(runnable);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

//        service.execute(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("peng","执行任务："+ indext+"   线程名字:"+Thread.currentThread().getName());
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

    }

    private void testThreadAnr() {

        try {
            int number = 0;
            while (number++ < 5) {
                Log.e(TAG, "主线程睡眠导致的ANR:次数" + number + "/5");
                try {
                    Thread.sleep(800L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
//                    LogUtils.e(TAG, "异常信息为:" + e.getMessage());
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
//            LogUtils.e(TAG, "异常信息为:" + e.getMessage());
        }
    }
}