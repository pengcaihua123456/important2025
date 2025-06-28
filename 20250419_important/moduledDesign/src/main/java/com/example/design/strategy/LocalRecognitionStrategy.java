package com.example.design.strategy;

// 本地离线识别策略
public class LocalRecognitionStrategy implements SpeechRecognitionStrategy {
    private final LocalSpeechRecognizer recognizer;

    public LocalRecognitionStrategy() {
        this.recognizer = new LocalSpeechRecognizer();
    }

    @Override
    public String recognize(byte[] audioData) {
        return recognizer.recognizeOffline(audioData);
    }

    @Override
    public String getStrategyName() {
        return "本地离线识别";
    }

    @Override
    public boolean isAvailable() {
        return true; // 本地引擎始终可用
    }
}