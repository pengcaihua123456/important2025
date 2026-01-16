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


public class CartFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        TextView textView = view.findViewById(R.id.text_cart);
        textView.setText("购物车\n\n目前有 3 件商品\n\n1. 智能手机 ×1\n2. 运动鞋 ×2\n\n总金额：¥ 599.00\n\n点击去结算");
        return view;
    }
}