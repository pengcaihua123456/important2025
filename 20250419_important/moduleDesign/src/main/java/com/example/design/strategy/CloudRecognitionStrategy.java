package com.example.design.strategy;

// 云端AI识别策略
public class CloudRecognitionStrategy implements SpeechRecognitionStrategy {
    private final CloudSpeechClient client;

    public CloudRecognitionStrategy(String apiKey) {
        this.client = new CloudSpeechClient(apiKey);
    }

    @Override
    public String recognize(byte[] audioData) {
        return client.recognizeOnline(audioData);
    }

    @Override
    public String getStrategyName() {
        return "云端AI识别";
    }

    @Override
    public boolean isAvailable() {
        return NetworkUtils.isNetworkAvailable(); // 需要网络
    }
}