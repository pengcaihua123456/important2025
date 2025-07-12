package com.example.design.compose.processing;

// 4. 策略模式 - 语音识别算法切换
public interface RecognitionStrategy {
    String recognize(byte[] audioData);
}

