package com.example.design.factory;

// 讯飞实现类（需了解具体构造细节）
public class IflytekRecognizerWithout {
    private String appId;

    public void setAppId(String id) { this.appId = id; }
    public void connectServer() { /* 建立讯飞专有连接 */ }
    public String recognize(byte[] data) {
        // 调用讯飞SDK
        return "Iflytek:识别结果";
    }
}

