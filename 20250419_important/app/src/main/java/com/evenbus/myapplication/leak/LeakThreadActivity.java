package com.evenbus.myapplication.leak;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LeakThreadActivity extends Activity {
    private List<String> list = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //模拟Activity一些其他的对象
        for(int i=0; i<10000;i++){
            list.add("Memory Leak!");
            Log.d("LeakThreadActivity",""+i);
        }
        //开启线程
        new MyThread().start();
    }

    // 错误示例3: 非静态内部类持有外部类引用
    public class MyThread extends Thread{

        @Override
        public void run() {
            super.run();

            //模拟耗时操作
            try {
                Log.d("LeakThreadActivity"," Thread.sleep start");
                Thread.sleep(1 * 60 * 1000);
                Log.d("LeakThreadActivity"," Thread.sleep end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
