package com.example.design.compose.adapter;

// 9. 适配器模式 - 第三方语音库集成
public class ThirdPartyRecognizer {
    public String recognize(byte[] input) {
        return "ThirdParty result: " + input.length + " bytes";
    }
}
