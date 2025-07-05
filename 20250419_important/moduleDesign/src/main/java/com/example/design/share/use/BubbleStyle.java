package com.example.design.share.use;

import com.example.design.share.MessageType;
import com.example.design.share.ResourceLoader;

// 1. 享元对象：气泡样式配置（内部状态）
public class BubbleStyle {
    final MessageType type;
    final String background; // 共享背景资源
    final int padding;       // 共享内边距
    final String icon;       // 共享图标

    public BubbleStyle(MessageType type) {
        this.type = type;
        // 只加载一次资源
        this.background = ResourceLoader.loadBackground(type);
        this.padding = ResourceLoader.loadPadding(type);
        this.icon = ResourceLoader.loadIcon(type);
        System.out.println("初始化气泡样式: " + type);
    }
}
