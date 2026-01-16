package com.taoduoduo.host.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.taoduoduo.shadow.R;


public class MineFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        TextView textView = view.findViewById(R.id.text_mine);
        textView.setText("我的淘多多\n\n用户名：张三\n\n会员等级：黄金会员\n\n优惠券：5张\n\n积分：1280\n\n订单：待付款(2) 待收货(1)\n\n收货地址管理\n\n客服中心");
        return view;
    }
}