package com.evenbus.myapplication.view.flow;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.evenbus.myapplication.R;

import java.util.Arrays;
import java.util.List;

public class FlowViewActivity extends Activity {

    private FlowLayout flowLayout;
    private List<String> tags = Arrays.asList(
            "Android", "Kotlin", "Java", "Flutter", "React Native",
            "iOS", "Swift", "Python", "机器学习", "人工智能",
            "大数据", "云计算", "区块链", "前端开发", "后端开发",
            "移动开发", "设计模式", "数据结构", "算法", "网络安全",
            "iOS", "Swift", "Python", "机器学习", "人工智能",
            "大数据", "云计算", "区块链", "前端开发", "后端开发",
            "移动开发", "设计模式", "数据结构", "算法", "网络安全"
    );

    private String[] colors = {
            "#FF4081", "#3F51B5", "#009688", "#795548", "#607D8B",
            "#E91E63", "#2196F3", "#4CAF50", "#FF9800", "#9C27B0",
            "#FF4081", "#3F51B5", "#009688", "#795548", "#607D8B",
            "#E91E63", "#2196F3", "#4CAF50", "#FF9800", "#9C27B0"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_view);

        ScrollViewFlowLayout scrollableFlowLayout = findViewById(R.id.scrollable_flow_layout);
        scrollableFlowLayout.setHorizontalSpacing(8);
        scrollableFlowLayout.setVerticalSpacing(8);


        for (int i = 0; i < tags.size(); i++) {
            scrollableFlowLayout.addTag(
                    tags.get(i),
                    Color.parseColor(colors[i % colors.length])
            );
        }


        ScrollableFlowLayout flowLayout = findViewById(R.id.scrollableflow_layout);
        flowLayout.setHorizontalSpacing(8);
        flowLayout.setVerticalSpacing(8);


        for (int i = 0; i < tags.size(); i++) {
            TextView textView = new TextView(this);
            textView.setText(tags.get(i));
            textView.setBackgroundColor(Color.parseColor(colors[i % colors.length]));
            textView.setPadding(24, 12, 24, 12);
            textView.setTextColor(Color.WHITE);

            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 4, 4, 4);

            flowLayout.addView(textView, params);


            flowLayoutInit();
        }
    }

    private void flowLayoutInit() {
        flowLayout = findViewById(R.id.flow_layout);
        flowLayout.setHorizontalSpacing(8); // 设置水平间距
        flowLayout.setVerticalSpacing(8);   // 设置垂直间距
        setupTags();
    }

    private void setupTags() {
        for (int i = 0; i < tags.size(); i++) {
            String tag = tags.get(i);
            TextView textView = (TextView) getLayoutInflater().inflate(
                    R.layout.item_tag, flowLayout, false);
            textView.setText(tag);

            // 设置不同的背景颜色
            textView.setBackgroundColor(Color.parseColor(colors[i % colors.length]));

            // 添加点击事件
            final int position = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTagClicked(tags.get(position));
                }
            });

            flowLayout.addView(textView);
        }
    }

    private void onTagClicked(String tag) {
        Toast.makeText(this, "点击了: " + tag, Toast.LENGTH_SHORT).show();
    }
}
