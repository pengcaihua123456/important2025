package com.example.design.strategy;

public class SpeechRecognizerWithoutPattern {
    private final LocalSpeechRecognizer localRecognizer;
    private final CloudSpeechClient cloudClient;
    private final MedicalTerminologyEnhancer medicalEnhancer;

    // 当前模式
    public static final int MODE_LOCAL = 0;
    public static final int MODE_CLOUD = 1;
    public static final int MODE_HYBRID = 2;
    public static final int MODE_MEDICAL = 3;

    private int currentMode = MODE_HYBRID;

    public SpeechRecognizerWithoutPattern() {
        localRecognizer = new LocalSpeechRecognizer();
        cloudClient = new CloudSpeechClient("API_KEY_HERE");
        medicalEnhancer = new MedicalTerminologyEnhancer();
    }

    public void setMode(int mode) {
        this.currentMode = mode;
    }

    public String recognizeSpeech(byte[] audioData) {
        switch (currentMode) {
            case MODE_LOCAL:
                return localRecognizer.recognizeOffline(audioData);

            case MODE_CLOUD:
                if (!NetworkUtils.isNetworkAvailable()) {
                    throw new IllegalStateException("网络不可用");
                }
                return cloudClient.recognizeOnline(audioData);

            case MODE_HYBRID:
                String localResult = localRecognizer.recognizeOffline(audioData);
                if (TextUtils.isEmpty(localResult) || localResult.equals("unknown")) {
                    if (NetworkUtils.isNetworkAvailable()) {
                        return cloudClient.recognizeOnline(audioData);
                    }
                }
                return localResult;

            case MODE_MEDICAL:
                if (!NetworkUtils.isNetworkAvailable()) {
                    throw new IllegalStateException("网络不可用");
                }
                String text = cloudClient.recognizeOnline(audioData);
                return medicalEnhancer.enhanceMedicalTerms(text);

            default:
                throw new IllegalStateException("未知模式");
        }
    }

    public String getCurrentModeName() {
        switch (currentMode) {
            case MODE_LOCAL: return "本地离线识别";
            case MODE_CLOUD: return "云端AI识别";
            case MODE_HYBRID: return "混合识别模式";
            case MODE_MEDICAL: return "医疗专用识别";
            default: return "未知模式";
        }
    }
}