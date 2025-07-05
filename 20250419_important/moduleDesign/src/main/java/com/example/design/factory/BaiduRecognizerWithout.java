package com.example.design.factory;

// 百度实现类（接口不一致）
public class BaiduRecognizerWithout {
    private String apiKey;

    public void setApiKey(String key) { this.apiKey = key; }
    public void authToken() { /* 百度鉴权流程 */ }
    public String processAudio(byte[] data) {
        // 调用百度SDK
        return "Baidu:识别结果";
    }
}
