package com.example.design.share;

import java.util.ArrayList;
import java.util.List;

// 聊天会话管理
public class ChatSession {
    private final List<BubbleView> messages = new ArrayList<>();

    public void addMessage(MessageType type, String content) {
        messages.add(new BubbleView(type, content));
    }

    public void render() {
        System.out.println("\n=== 渲染聊天页面 ===");
        for (BubbleView bubble : messages) {
            bubble.display();
        }
    }
}

