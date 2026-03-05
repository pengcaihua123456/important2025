package com.taoduoduo.host;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;



import com.tencent.shadow.sample.host.R;

public class HomeActivity extends AppCompatActivity { // 使用 AppCompatActivity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 获取传递的参数
        String from = getIntent().getStringExtra("from");
        long timestamp = getIntent().getLongExtra("timestamp", 0);

        // 创建Fragment实例并传递参数
        Fragment fragment = new Fragment();
        Bundle args = new Bundle();
        args.putString("from", from != null ? from : "HomeActivity");
        args.putLong("timestamp", timestamp);
        fragment.setArguments(args);

        // 添加Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // 处理返回按钮点击
        return true;
    }
}