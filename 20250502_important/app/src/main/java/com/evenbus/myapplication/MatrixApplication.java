package com.evenbus.myapplication;

import android.app.Application;
import android.content.Context;
import android.util.Log;


import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import java.io.File;

public class MatrixApplication  extends MultiDexApplication {

    private static final String TAG = "Matrix.Application";

    @Override
    public void onCreate() {
        super.onCreate();
        // 确保类被加载
        try {
            Class.forName("androidx.multidex.MultiDexApplication");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MultiDex初始化失败", e);
        }
        init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG,"attachBaseContext");
//        MultiDex.install(this);
    }
    public  void init(Application application){

    }


}
