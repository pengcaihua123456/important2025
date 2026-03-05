package com.tencent.shadow.sample.plugin;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// 修改为继承 FragmentActivity
public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 添加 PluginDoubleElevenFragment
        addDoubleElevenFragment();
    }

    private void addDoubleElevenFragment() {
        // 获取 FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();

        // 开始 Fragment 事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 创建 Fragment 实例
        PluginDoubleElevenFragment fragment = PluginDoubleElevenFragment.newInstance();

        // 添加 Fragment 到容器中（需要先在布局中添加容器）
        transaction.add(R.id.fragment_container, fragment);

        // 提交事务
        transaction.commit();
    }
}