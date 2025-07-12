package com.example.design.compose.observer;

// 5. 观察者模式 - 语音识别结果通知
public interface RecognitionListener {
    void onRecognitionResult(String result);
    void onError(String error);
}
