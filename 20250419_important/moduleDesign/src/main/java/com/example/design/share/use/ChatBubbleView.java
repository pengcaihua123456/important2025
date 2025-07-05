package com.example.design.share.use;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.design.share.MessageType;

// 实际Android中的气泡视图（简化版）
public class ChatBubbleView extends FrameLayout {
    private BubbleStyle style;
    private TextView contentView;
    private ImageView iconView;

    public ChatBubbleView(Context context, MessageType type, String content) {
        super(context);
        init(type, content);
    }

    private void init(MessageType type, String content) {
        // 获取共享样式
        style = BubbleStyleFactory.getStyle(type);

        // 设置背景（实际项目中加载Drawable）
        setBackgroundResource(getResId(style.background));

        // 创建内嵌视图
        contentView = new TextView(getContext());
        contentView.setText(content);
        contentView.setPadding(style.padding, style.padding, style.padding, style.padding);

        iconView = new ImageView(getContext());
        iconView.setImageResource(getResId(style.icon));

        // 添加视图
        addView(iconView);
        addView(contentView);
    }

    private int getResId(String resName) {
        // 实际项目中通过资源名获取ID
        return getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
    }


    public void updateContent(String msg) {
    }
}
