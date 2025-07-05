package com.example.design.proxy;

public class BasicVoiceClient {
    public static void main(String[] args) {
        BasicVoiceRecognizer recognizer = new BasicVoiceRecognizer();

        try {
            // 设置语言
            recognizer.setLanguage("en-US");

            // 模拟多次识别调用
            for (int i = 0; i < 3; i++) {
                String audioData = "audio_sample_" + System.currentTimeMillis();
                String result = recognizer.recognize(audioData);
                System.out.println("Recognition Result: " + result);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("Recognition failed: " + e.getMessage());
        }
    }
}