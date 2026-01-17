package com.taoduoduo.common;



import android.os.Bundle;

import androidx.fragment.app.Fragment;

/**
 * 插件UI提供者接口
 * 宿主通过此接口获取插件的Fragment
 */
public interface IPluginUiProvider {

    /**
     * 获取Fragment类名
     */
    String getFragmentClassName();

    /**
     * 创建Fragment实例
     */
    Fragment createFragment();

    /**
     * 创建Fragment实例（带参数）
     */
    Fragment createFragment(Bundle args);

    /**
     * 获取插件版本
     */
    String getPluginVersion();

    /**
     * 获取插件名称
     */
    String getPluginName();
}