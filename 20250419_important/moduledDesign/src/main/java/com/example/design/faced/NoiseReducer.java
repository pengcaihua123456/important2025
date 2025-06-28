package com.example.design.faced;

public class NoiseReducer {
    public byte[] process(byte[] rawAudio) {
        System.out.println("[NoiseReducer] 降噪处理中...");
        return rawAudio; // 返回处理后的音频
    }
}