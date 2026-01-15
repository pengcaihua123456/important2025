package com.example.matrixdemo.app;

import android.app.Application;
import android.util.Log;

import com.example.matrixdemo.utils.MatrixUtils;


/**
 * @author: njb
 * @date: 2023/8/10 11:31
 * @desc:
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("pengcaihua", "------onCreate------" );
        initMatrix();
    }

    private void initMatrix() {
        MatrixUtils.getInstance().initPlugin(this,"com.example.matrixdemo.MainActivity;");
    }
}
