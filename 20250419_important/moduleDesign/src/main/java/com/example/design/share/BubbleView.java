package com.example.design.share;

// 消息气泡视图（非享元模式）
class BubbleView {
    private final MessageType type;
    private final String background; // 背景资源
    private final int padding;       // 内边距
    private final String icon;       // 图标资源
    private final String content;    // 内容

    public BubbleView(MessageType type, String content) {
        this.type = type;
        // 每次创建都加载资源（高开销操作）
        this.background = ResourceLoader.loadBackground(type);
        this.padding = ResourceLoader.loadPadding(type);
        this.icon = ResourceLoader.loadIcon(type);
        this.content = content;
        System.out.println("创建新气泡: " + type + " | 背景: " + background);
    }

    public void display() {
        System.out.printf("显示[%s]气泡: %s (内边距: %dpx, 图标: %s)\n",
                type, content, padding, icon);
    }
}
