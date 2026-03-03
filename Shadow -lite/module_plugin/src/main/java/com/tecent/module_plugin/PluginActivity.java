package com.tecent.module_plugin;

import android.os.Bundle;

import com.example.module_route.R;

public class PluginActivity extends ShadowActivity {

    @Override
    public void onCreate_Inner(Bundle savedInstanceState) {
        super.onCreate_Inner(savedInstanceState);
        // 这里的 R.layout 是插件工程的 R，setContentView 委托给了宿主
        // 关键点：宿主容器的 getResources 必须被替换为插件的 Resources，否则找不到插件的 layout
//        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStart_Inner() {
        // 业务逻辑
    }
}
