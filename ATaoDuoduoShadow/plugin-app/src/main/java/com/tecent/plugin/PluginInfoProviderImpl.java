package com.tecent.plugin;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.tencent.shadow.common.IPluginInfoProvider;
import com.tencent.shadow.sample.plugin.PluginDoubleElevenFragment;

import java.util.Arrays;
import java.util.List;

public class PluginInfoProviderImpl implements IPluginInfoProvider {

    public static final String TAG ="PluginInfoProviderImpl";
    @Override
    public String getPluginName() {
        Log.d(TAG,"getPluginName");
        return "示例插件";
    }

    @Override
    public String getPluginVersion() {
        Log.d(TAG,"getPluginVersion");
        return "1.0.0";
    }

    @Override
    public List<String> getFeatureList() {
        Log.d(TAG,"getFeatureList");
        return Arrays.asList("功能A", "功能B", "功能C");
    }

    @Override
    public Bundle getPluginData(String key) {
        Log.d(TAG,"getPluginData");
        Bundle bundle = new Bundle();
        if ("user_info".equals(key)) {
            bundle.putString("name", "测试用户");
            bundle.putInt("level", 5);
        }
        return bundle;
    }

    @Override
    public Fragment getFragment() {
        Log.d(TAG,"getFragment");
        return new PluginDoubleElevenFragment();
    }
}