package com.example.design.share;

import com.example.design.share.use.OptimizedChatSession;

// 测试客户端
public class NonFlyweightDemo {
    public static void main(String[] args) {
        ChatSession session = new ChatSession();

        // 添加多条消息（包含重复类型）
        session.addMessage(MessageType.TEXT, "你好！");
        session.addMessage(MessageType.IMAGE, "[图片]风景照");
        session.addMessage(MessageType.TEXT, "今天天气不错");
        session.addMessage(MessageType.VOICE, "语音消息 15");
                session.addMessage(MessageType.TEXT, "晚上一起吃饭吗？");
        session.addMessage(MessageType.IMAGE, "[图片]美食");
        session.addMessage(MessageType.TEXT, "好的，6点见");

        session.render();

        // 分析：每条消息都创建了完整的气泡对象
        // 包括重复加载相同的资源
    }

    public static void main2(String[] args) {
        OptimizedChatSession session = new OptimizedChatSession();

        // 添加相同的消息
        session.addMessage(MessageType.TEXT, "你好！");
        session.addMessage(MessageType.IMAGE, "[图片]风景照");
        session.addMessage(MessageType.TEXT, "今天天气不错");
        session.addMessage(MessageType.VOICE, "语音消息 15");
                session.addMessage(MessageType.TEXT, "晚上一起吃饭吗？");
        session.addMessage(MessageType.IMAGE, "[图片]美食");
        session.addMessage(MessageType.TEXT, "好的，6点见");

        session.render();

        // 添加更多消息测试缓存效果
        System.out.println("\n添加新消息...");
        session.addMessage(MessageType.VOICE, "新语音消息 8");
                session.addMessage(MessageType.TEXT, "收到！");
        session.render();
    }
}
