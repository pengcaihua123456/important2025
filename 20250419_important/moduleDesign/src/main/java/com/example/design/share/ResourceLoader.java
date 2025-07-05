package com.example.design.share;

// 模拟资源加载工具类
public class ResourceLoader {
    // 获取气泡背景资源（模拟实际项目中的资源加载）
    public static String loadBackground(MessageType type) {
        switch (type) {
            case TEXT: return "text_bubble_bg.9.png";
            case IMAGE: return "image_bubble_bg.9.png";
            case VOICE: return "voice_bubble_bg.9.png";
            default: return "default_bubble.9.png";
        }
    }

    // 获取内边距配置
    public static int loadPadding(MessageType type) {
        switch (type) {
            case TEXT: return 16;
            case IMAGE: return 8;
            case VOICE: return 12;
            default: return 10;
        }
    }

    // 获取图标资源
    public  static String loadIcon(MessageType type) {
        switch (type) {
            case TEXT: return "ic_text.png";
            case IMAGE: return "ic_image.png";
            case VOICE: return "ic_voice.png";
            default: return "ic_default.png";
        }
    }
}

