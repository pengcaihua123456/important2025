package com.example.design.faced;

public class SpeechRecognizer {
    public String recognize(byte[] audio) {
        System.out.println("[SpeechRecognizer] 语音识别中...");
        // 模拟10%概率识别失败
        if (Math.random() > 0.9) {
            throw new VoiceException(
                    VoiceException.ErrorType.RECOGNITION,
                    "语音不清晰"
            );
        }
        return "打开客厅的灯"; // 模拟识别结果
    }
}
