package com.tencent.host.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class PluginManagerImpl {

    private Resources pluginResources;
    private DexClassLoader pluginClassLoader;
    private Context hostContext;

    private static class Holder {
        private static final PluginManagerImpl INSTANCE = new PluginManagerImpl();
    }

    public static PluginManagerImpl getInstance() {
        return Holder.INSTANCE;
    }

    public void setContext(Context context) {
        this.hostContext = context;
    }

    public Resources getPluginResources() {
        return pluginResources;
    }

    public DexClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

    /**
     * 加载插件 APK
     * @param apkPath 插件 APK 的绝对路径
     */
    public void loadPlugin(String apkPath) {
        if (hostContext == null) return;

        // 1. 初始化 DexClassLoader
        File dexOutputDir = hostContext.getDir("dex", Context.MODE_PRIVATE);
        pluginClassLoader = new DexClassLoader(
                apkPath,
                dexOutputDir.getAbsolutePath(),
                null,
                hostContext.getClassLoader()
        );

        // 2. 初始化 Resources (核心难点：反射创建 AssetManager)
        try {
            AssetManager assetManager = createAssetManager(apkPath);
            pluginResources = new Resources(
                    assetManager,
                    hostContext.getResources().getDisplayMetrics(),
                    hostContext.getResources().getConfiguration()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Load plugin resources failed", e);
        }
    }

    /**
     * 【核心魔法 1】反射创建指向特定 APK 的 AssetManager
     */
    private AssetManager createAssetManager(String apkPath) throws Exception {
        AssetManager assetManager = AssetManager.class.newInstance();
        Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
        addAssetPathMethod.setAccessible(true);
        int cookie = (int) addAssetPathMethod.invoke(assetManager, apkPath);
        if (cookie == 0) {
            throw new RuntimeException("Failed to add asset path: " + apkPath);
        }
        return assetManager;
    }
}