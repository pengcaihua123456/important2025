package com.example.design.share.use;

import com.example.design.share.MessageType;

// 3. 气泡视图（使用享元对象）
class FlyweightBubbleView {
    private final BubbleStyle style; // 共享样式
    private final String content;    // 外部状态

    public FlyweightBubbleView(MessageType type, String content) {
        this.style = BubbleStyleFactory.getStyle(type);
        this.content = content;
    }

    public void display() {
        System.out.printf("显示[%s]气泡: %s (内边距: %dpx, 图标: %s)\n",
                style.type, content, style.padding, style.icon);
    }
}
