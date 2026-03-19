package com.tencent.shadow.common;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.List;

// 插件模块 - 定义的接口（建议放在单独的模块，宿主和插件共用）
public interface IPluginInfoProvider {
    String getPluginName();
    String getPluginVersion();
    List<String> getFeatureList();
    Bundle getPluginData(String key);

    Fragment getFragment();

    void setHostService(IHostService service);
}

