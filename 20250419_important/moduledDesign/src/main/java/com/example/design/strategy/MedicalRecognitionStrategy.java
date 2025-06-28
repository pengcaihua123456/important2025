package com.example.design.strategy;

// 医疗领域专用识别策略
public class MedicalRecognitionStrategy implements SpeechRecognitionStrategy {
    private final CloudSpeechClient client;
    private final MedicalTerminologyEnhancer enhancer;

    public MedicalRecognitionStrategy(String apiKey) {
        this.client = new CloudSpeechClient(apiKey);
        this.enhancer = new MedicalTerminologyEnhancer();
    }

    @Override
    public String recognize(byte[] audioData) {
        String text = client.recognizeOnline(audioData);
        return enhancer.enhanceMedicalTerms(text);
    }

    @Override
    public String getStrategyName() {
        return "医疗专用识别";
    }

    @Override
    public boolean isAvailable() {
        return NetworkUtils.isNetworkAvailable();
    }
}
