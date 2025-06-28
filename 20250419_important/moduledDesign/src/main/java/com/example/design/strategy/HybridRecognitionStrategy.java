package com.example.design.strategy;

// 混合识别策略
public class HybridRecognitionStrategy implements SpeechRecognitionStrategy {
    private final LocalRecognitionStrategy localStrategy;
    private final CloudRecognitionStrategy cloudStrategy;

    public HybridRecognitionStrategy(String apiKey) {
        this.localStrategy = new LocalRecognitionStrategy();
        this.cloudStrategy = new CloudRecognitionStrategy(apiKey);
    }

    @Override
    public String recognize(byte[] audioData) {
        // 先尝试本地识别
        String result = localStrategy.recognize(audioData);
        if (result.equals("unknown")) {
            // 本地识别失败，尝试云端
            if (cloudStrategy.isAvailable()) {
                result = cloudStrategy.recognize(audioData);
            }
        }
        return result;
    }

    @Override
    public String getStrategyName() {
        return "混合识别模式";
    }

    @Override
    public boolean isAvailable() {
        return localStrategy.isAvailable() || cloudStrategy.isAvailable();
    }
}