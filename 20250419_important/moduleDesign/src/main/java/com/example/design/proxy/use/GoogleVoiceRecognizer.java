package com.example.design.proxy.use;

// 2. 真实对象 - 实现核心业务逻辑
public class GoogleVoiceRecognizer implements IVoiceRecognizer {
    private String currentLanguage = "zh-CN";

    @Override
    public String recognize(String audioData) {
        // 模拟实际语音识别处理（核心业务）
        System.out.println("Processing audio data: " + audioData.substring(0, 10) + "...");
        return "识别结果: " + audioData.hashCode();
    }

    @Override
    public void setLanguage(String language) {
        this.currentLanguage = language;
        System.out.println("Language set to: " + language);
    }
}