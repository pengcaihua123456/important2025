package com.tencent.host.core;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.module_route.lib.LifecyclerInterface;

public class PluginContainerActivity extends Activity {

    private static final String TARGET_CLASS_NAME = "com.example.plugin.PluginActivity";
    private LifecyclerInterface pluginInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ClassLoader pluginLoader = PluginManagerImpl.getInstance().getPluginClassLoader();
            if (pluginLoader == null) {
                PluginManagerImpl.getInstance().loadPlugin("/sdcard/plugin.apk");
                pluginLoader = PluginManagerImpl.getInstance().getPluginClassLoader();
            }

            Class<?> clazz = pluginLoader.loadClass(TARGET_CLASS_NAME);
            Object obj = clazz.getDeclaredConstructor().newInstance();

            if (obj instanceof LifecyclerInterface) {
                pluginInstance = (LifecyclerInterface) obj;
                pluginInstance.attach_Inner(this);
                pluginInstance.onCreate_Inner(savedInstanceState);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Load Plugin Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pluginInstance != null) pluginInstance.onStart_Inner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pluginInstance != null) pluginInstance.onResume_Inner();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pluginInstance != null) pluginInstance.onPause_Inner();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pluginInstance != null) pluginInstance.onStop_Inner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pluginInstance != null) pluginInstance.onDestroy_Inner();
    }

    // ================= 【核心魔法 2 & 3】关键重写 =================

    @Override
    public ClassLoader getClassLoader() {
        ClassLoader loader = PluginManagerImpl.getInstance().getPluginClassLoader();
        return loader != null ? loader : super.getClassLoader();
    }

    @Override
    public Resources getResources() {
        Resources res = PluginManagerImpl.getInstance().getPluginResources();
        return res != null ? res : super.getResources();
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        Resources res = getResources();
        LayoutInflater inflater = super.getLayoutInflater();
        return inflater;
    }
}