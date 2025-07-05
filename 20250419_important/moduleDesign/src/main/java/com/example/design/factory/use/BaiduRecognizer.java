package com.example.design.factory.use;

public class BaiduRecognizer implements SpeechRecognizer {
    private String apiKey;

    @Override
    public void initialize(String config) {
        this.apiKey = config;
        this.authToken();
    }

    private void authToken() { /* 百度鉴权 */ }

    @Override
    public String recognize(byte[] audioData) {
        // 调用百度SDK（实际代码）
        return "Baidu:识别结果";
    }
}
