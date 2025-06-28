package com.example.design.strategy;

import java.util.HashMap;
import java.util.Map;

// 语音识别上下文
public class SpeechRecognitionContext {
    private SpeechRecognitionStrategy strategy;
    private final Map<String, SpeechRecognitionStrategy> strategyCache = new HashMap<>();

    public SpeechRecognitionContext() {
        // 初始化默认策略
        setStrategy("hybrid");
    }

    public void setStrategy(String strategyKey) {
        if (strategyCache.containsKey(strategyKey)) {
            strategy = strategyCache.get(strategyKey);
            return;
        }

        switch (strategyKey) {
            case "local":
                strategy = new LocalRecognitionStrategy();
                break;
            case "cloud":
                strategy = new CloudRecognitionStrategy("API_KEY_HERE");
                break;
            case "hybrid":
                strategy = new HybridRecognitionStrategy("API_KEY_HERE");
                break;
            case "medical":
                strategy = new MedicalRecognitionStrategy("MEDICAL_API_KEY");
                break;
            default:
                throw new IllegalArgumentException("未知策略: " + strategyKey);
        }

        strategyCache.put(strategyKey, strategy);
    }

    public String recognizeSpeech(byte[] audioData) {
        if (strategy == null || !strategy.isAvailable()) {
            throw new IllegalStateException("没有可用的语音识别策略");
        }
        return strategy.recognize(audioData);
    }

    public String getCurrentStrategyName() {
        return strategy != null ? strategy.getStrategyName() : "未设置策略";
    }
}