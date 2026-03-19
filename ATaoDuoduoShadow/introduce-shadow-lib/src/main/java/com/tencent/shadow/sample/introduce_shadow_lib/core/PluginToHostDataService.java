package com.tencent.shadow.sample.introduce_shadow_lib.core;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.tencent.shadow.common.IPluginInfoProvider;

/**
 * 插件信息工具类 - 静态方法版本
 * 通过ClassLoader直接调用插件方法
 */
public class PluginToHostDataService {
    private static final String TAG = "PluginInfoUtils";

    // 默认的Provider类名（作为备用）
    private static final String DEFAULT_PROVIDER_CLASS_NAME = "com.tecent.plugin.PluginInfoProviderImpl";

    /**
     * 从ClassLoader获取插件名称（使用默认类名）
     * @param classLoader 插件的ClassLoader
     * @return 插件名称
     */
    public static String getPluginName(ClassLoader classLoader) {
        return getPluginName(classLoader, DEFAULT_PROVIDER_CLASS_NAME);
    }

    /**
     * 从ClassLoader获取插件名称（指定Provider类名）
     * @param classLoader 插件的ClassLoader
     * @param providerClassName Provider类的完整类名
     * @return 插件名称
     */
    public static String getPluginName(ClassLoader classLoader, String providerClassName) {
        IPluginInfoProvider provider = getProvider(classLoader, providerClassName);
        if (provider == null) {
            return "未知插件";
        }

        try {
            return provider.getPluginName();
        } catch (Exception e) {
            Log.e(TAG, "获取插件名称失败", e);
            return "获取失败";
        }
    }

    /**
     * 从ClassLoader获取插件版本（使用默认类名）
     */
    public static String getPluginVersion(ClassLoader classLoader) {
        return getPluginVersion(classLoader, DEFAULT_PROVIDER_CLASS_NAME);
    }

    /**
     * 从ClassLoader获取插件版本（指定Provider类名）
     */
    public static String getPluginVersion(ClassLoader classLoader, String providerClassName) {
        IPluginInfoProvider provider = getProvider(classLoader, providerClassName);
        if (provider == null) {
            return "未知版本";
        }

        try {
            return provider.getPluginVersion();
        } catch (Exception e) {
            Log.e(TAG, "获取插件版本失败", e);
            return "获取失败";
        }
    }

    /**
     * 从ClassLoader获取插件Fragment（使用默认类名）
     */
    public static Fragment getPluginFragment(ClassLoader classLoader) {
        return getPluginFragment(classLoader, DEFAULT_PROVIDER_CLASS_NAME);
    }

    /**
     * 从ClassLoader获取插件Fragment（指定Provider类名）
     */
    public static Fragment getPluginFragment(ClassLoader classLoader, String providerClassName) {
        IPluginInfoProvider provider = getProvider(classLoader, providerClassName);
        if (provider == null) {
            return null;
        }

        try {
            return provider.getFragment();
        } catch (Exception e) {
            Log.e(TAG, "获取插件Fragment失败", e);
            return null;
        }
    }

    /**
     * 通用的获取Provider实例的方法
     * @param classLoader 插件的ClassLoader
     * @param providerClassName Provider类的完整类名
     * @return IPluginInfoProvider实例
     */
    public static IPluginInfoProvider getProvider(ClassLoader classLoader, String providerClassName) {
        if (classLoader == null) {
            Log.e(TAG, "ClassLoader为空");
            return null;
        }

        if (providerClassName == null || providerClassName.isEmpty()) {
            Log.e(TAG, "Provider类名为空");
            return null;
        }

        try {
            Class<?> providerClass = classLoader.loadClass(providerClassName);
            Object instance = providerClass.newInstance();

            if (instance instanceof IPluginInfoProvider) {
                return (IPluginInfoProvider) instance;
            } else {
                Log.e(TAG, "类 " + providerClassName + " 未实现IPluginInfoProvider接口");
                return null;
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "未找到类: " + providerClassName, e);
            return null;
        } catch (IllegalAccessException | InstantiationException e) {
            Log.e(TAG, "创建实例失败: " + providerClassName, e);
            return null;
        }
    }

    /**
     * 带缓存版本的获取Provider（使用默认类名）
     */
    public static IPluginInfoProvider getProviderWithCache(ClassLoader classLoader) {
        return getProviderWithCache(classLoader, DEFAULT_PROVIDER_CLASS_NAME);
    }

    /**
     * 带缓存版本的获取Provider（指定Provider类名）
     */
    public static IPluginInfoProvider getProviderWithCache(ClassLoader classLoader, String providerClassName) {
        // 使用组合键：ClassLoader + 类名作为缓存key
        String cacheKey = classLoader != null ? classLoader.toString() + "_" + providerClassName : null;

        // 简单的缓存实现，实际使用时可以考虑使用Map
        if (sCachedClassLoader == classLoader &&
                sCachedProviderClassName != null &&
                sCachedProviderClassName.equals(providerClassName) &&
                sCachedProvider != null) {
            return sCachedProvider;
        }

        sCachedProvider = getProvider(classLoader, providerClassName);
        sCachedClassLoader = classLoader;
        sCachedProviderClassName = providerClassName;
        return sCachedProvider;
    }

    private static IPluginInfoProvider sCachedProvider;
    private static ClassLoader sCachedClassLoader;
    private static String sCachedProviderClassName;
}