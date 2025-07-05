package com.example.design.factory.use;

// ===== 1. 统一定义产品接口 =====
public interface SpeechRecognizer {
    void initialize(String config); // 统一初始化
    String recognize(byte[] audioData); // 统一识别方法
}
