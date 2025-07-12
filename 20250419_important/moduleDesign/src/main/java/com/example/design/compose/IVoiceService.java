package com.example.design.compose;

// 7. 动态代理 - 语音服务日志记录
public interface IVoiceService {
    void processVoice(byte[] data);
    String synthesizeText(String text);
}
