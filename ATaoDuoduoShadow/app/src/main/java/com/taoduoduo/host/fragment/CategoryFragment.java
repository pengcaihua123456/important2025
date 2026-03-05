package com.taoduoduo.host.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.taoduoduo.host.CategoryDetailActivity;
import com.tencent.shadow.sample.host.R;


public class CategoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        TextView textView = view.findViewById(R.id.text_category);
        textView.setText("商品分类\n\n• 电子产品\n• 服装服饰\n• 家居用品\n• 美妆护肤\n• 图书音像\n• 运动户外\n• 食品饮料");

        // 设置点击事件
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CategoryDetailActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}