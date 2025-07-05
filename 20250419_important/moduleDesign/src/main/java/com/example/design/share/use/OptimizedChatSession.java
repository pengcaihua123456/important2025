package com.example.design.share.use;

import com.example.design.share.MessageType;

import java.util.ArrayList;
import java.util.List;

// 优化的聊天会话管理
public class OptimizedChatSession {
    private final List<FlyweightBubbleView> messages = new ArrayList<>();

    public void addMessage(MessageType type, String content) {
        messages.add(new FlyweightBubbleView(type, content));
    }

    public void render() {
        System.out.println("\n=== 渲染聊天页面（享元模式） ===");
        for (FlyweightBubbleView bubble : messages) {
            bubble.display();
        }
        System.out.println(BubbleStyleFactory.getCacheStatus());
    }
}
