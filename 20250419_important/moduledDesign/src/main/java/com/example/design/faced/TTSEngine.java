package com.example.design.faced;

public class TTSEngine {
    public byte[] synthesize(String text) {
        System.out.println("[TTSEngine] 语音合成中: + text ");
        return text.getBytes();
    }
}