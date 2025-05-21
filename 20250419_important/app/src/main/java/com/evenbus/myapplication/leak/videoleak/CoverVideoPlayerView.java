package com.evenbus.myapplication.leak.videoleak;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.Toast;

// 自定义View - CoverVideoPlayerView.java
public class CoverVideoPlayerView extends FrameLayout {
    private Context mContext;
    private NetChangeListener mNetChangeListener;
    private NetInfoModule netInfoModule;

    public CoverVideoPlayerView(Context context) {
        this(context, null);
    }

    public CoverVideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        // 初始化网络监听模块
        netInfoModule = new NetInfoModule();
        // 创建匿名内部类监听器
        mNetChangeListener = new NetChangeListener() {
            @Override
            public void onNetChange(boolean isAvailable) {
                // 处理网络变化
                if (mContext != null) {
                    Toast.makeText(mContext, "网络状态变化:" + isAvailable, Toast.LENGTH_SHORT).show();
                }
            }
        };
        // 注册监听
        netInfoModule.registerNetChangeListener(mNetChangeListener,mContext);
    }

    // 释放资源方法（但开发者可能忘记调用）
    public void release() {
        if (netInfoModule != null) {
            netInfoModule.unregisterNetChangeListener(mNetChangeListener,mContext);
        }
    }
}


// 网络变化监听接口
