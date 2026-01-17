package com.taoduoduo.host;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.taoduoduo.host.adapter.ViewPagerAdapter;
import com.tencent.shadow.sample.host.R;


public class TaoduoduoMainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private String[] tabTitles = {"首页", "双11", "分类", "购物车", "我的"};
    private int[] tabIcons = {
            android.R.drawable.ic_menu_call,
            android.R.drawable.star_big_on,
            android.R.drawable.ic_menu_sort_by_size,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_my_calendar
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置Toolbar
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // 初始化ViewPager2
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(this));

        // 初始化TabLayout
        tabLayout = findViewById(R.id.tab_layout);

        // 设置Tab
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(tabTitles[position]);
                        // 安全地设置图标
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            tab.setIcon(tabIcons[position]);
                        }
                    }
                }
        ).attach();
    }
}