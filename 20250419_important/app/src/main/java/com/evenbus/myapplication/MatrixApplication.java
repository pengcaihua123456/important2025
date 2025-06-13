package com.evenbus.myapplication;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;

public class MatrixApplication extends Application {

    private static final String TAG = "Matrix.Application";

    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.debuggable();
        ARouter.init(this);



    }


}
