package com.example.design.share.use;

import com.example.design.share.MessageType;

import java.util.EnumMap;
import java.util.Map;

// 2. 享元工厂
class BubbleStyleFactory {
    private static final Map<MessageType, BubbleStyle> stylePool = new EnumMap<>(MessageType.class);

    public static BubbleStyle getStyle(MessageType type) {
        return stylePool.computeIfAbsent(type, BubbleStyle::new);
    }

    // 获取当前缓存状态（用于监控）
    public static String getCacheStatus() {
        return "缓存样式数: " + stylePool.size() + "/" + MessageType.values().length;
    }
}

