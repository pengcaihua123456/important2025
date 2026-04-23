package com.evenbus.myapplication;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.evenbus.myapplication.leak.DefaultInitTask;
import com.evenbus.myapplication.leak.OOMMonitorInitTask;

public class MatrixApplication extends Application {

    private static final String TAG = "Matrix.Application";

    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.debuggable();
        ARouter.init(this);


        // 1. 先初始化 DefaultInitTask
        DefaultInitTask.init(this);
        // 2. 再初始化 OOMMonitorInitTask
        OOMMonitorInitTask.getInstance().init(this);

//        new KOOMConfig().init(this);


//        new MatrixUtil().init(this);

    }


}
