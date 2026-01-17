package com.tencent.shadow.sample.plugin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;




public class PluginDoubleElevenFragment extends Fragment {

    public PluginDoubleElevenFragment() {
        // 空的构造函数
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_double_eleven, container, false);
        TextView textView = view.findViewById(R.id.text_double_eleven);
        textView.setText("双11狂欢节\n\n限时抢购中！\n\n超多优惠等你来！\n\n全场5折起");
        return view;
    }

    // 如果需要传递参数，可以添加以下方法
    public static PluginDoubleElevenFragment newInstance() {
        return new PluginDoubleElevenFragment();
    }

    public static PluginDoubleElevenFragment newInstance(String param) {
        PluginDoubleElevenFragment fragment = new PluginDoubleElevenFragment();
        Bundle args = new Bundle();
        args.putString("param_key", param);
        fragment.setArguments(args);
        return fragment;
    }
}