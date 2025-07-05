package com.example.design.proxy.use;

// 1. 定义核心接口
public interface IVoiceRecognizer {
    String recognize(String audioData);  // 语音识别方法
    void setLanguage(String language);   // 设置识别语言
}