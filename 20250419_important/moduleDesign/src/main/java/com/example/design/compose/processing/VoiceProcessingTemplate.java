package com.example.design.compose.processing;

import java.util.Arrays;

// 8. 模板方法 - 语音处理流程
public abstract class VoiceProcessingTemplate {
    public final String process(byte[] audio) {
        byte[] cleaned = preProcess(audio);
        String text = recognize(cleaned);
        String response = handleCommand(text);
        return synthesizeResponse(response);
    }

    protected byte[] preProcess(byte[] audio) {
        System.out.println("Applying noise reduction...");
        // 模拟降噪处理
        return Arrays.copyOf(audio, audio.length);
    }

    protected abstract String recognize(byte[] audio);

    protected String handleCommand(String text) {
        System.out.println("Handling command: " + text);
        // 这里会连接责任链处理命令
        return "Processed: " + text;
    }

    protected String synthesizeResponse(String response) {
        System.out.println("Synthesizing response...");
        return response;
    }
}

