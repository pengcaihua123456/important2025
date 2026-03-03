package com.example.module_route.lib;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;

public interface LifecyclerInterface {

    // 绑定宿主上下文（关键：让插件获取到宿主的 Context）
    void attach_Inner(Activity hostActivity);

    // 模拟生命周期回调
    void onCreate_Inner(Bundle savedInstanceState);
    void onStart_Inner();
    void onResume_Inner();
    void onPause_Inner();
    void onStop_Inner();
    void onDestroy_Inner();

    // 常用能力代理
    LayoutInflater getLayoutInflater_Inner();
}