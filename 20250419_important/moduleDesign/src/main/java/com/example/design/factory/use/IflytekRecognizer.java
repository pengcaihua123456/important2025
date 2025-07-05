package com.example.design.factory.use;

// ===== 2. 实现具体产品 =====
public class IflytekRecognizer implements SpeechRecognizer {
    private String appId;

    @Override
    public void initialize(String config) {
        this.appId = config;
        this.connectServer();
    }

    private void connectServer() { /* 讯飞专有连接 */ }

    @Override
    public String recognize(byte[] audioData) {
        // 调用讯飞SDK（实际代码）
        return "Iflytek:识别结果";
    }
}
