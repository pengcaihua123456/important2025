package com.tecent.module_plugin;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;

import com.example.module_route.lib.LifecyclerInterface;

public class ShadowActivity implements LifecyclerInterface {

    // 宿主容器 Activity，插件的所有 Context 相关操作都委托给它
    protected Activity hostActivity;

    @Override
    public void attach_Inner(Activity activity) {
        this.hostActivity = activity;
    }

    // --- 生命周期空实现，供子类重写 ---
    @Override
    public void onCreate_Inner(Bundle save) {}
    @Override
    public void onStart_Inner() {}
    @Override
    public void onResume_Inner() {}
    @Override
    public void onPause_Inner() {}
    @Override
    public void onStop_Inner() {}
    @Override
    public void onDestroy_Inner() {}

    // --- 上下文能力代理 ---
    @Override
    public LayoutInflater getLayoutInflater_Inner() {
        if (hostActivity != null) {
            return hostActivity.getLayoutInflater();
        }
        return null;
    }

    // 代理其他常用方法，确保插件代码以为自己在 Activity 环境中运行
    public ApplicationInfo getApplicationInfo() {
        return hostActivity.getApplicationInfo();
    }

    public Resources getResources() {
        // 注意：这里应该返回插件的 Resources，但在简单版中，
        // 我们通过重写宿主容器的 getResources 来间接实现，或者在此处做特殊处理
        return hostActivity.getResources();
    }

    public Window getWindow() {
        return hostActivity.getWindow();
    }

    public void setContentView(int layoutResID) {
        if (hostActivity != null) {
            hostActivity.setContentView(layoutResID);
        }
    }
}