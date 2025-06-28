package com.example.design.faced;

// ===== 新增：语音异常类 =====
public class VoiceException extends RuntimeException {
    private final ErrorType type;

    public VoiceException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public ErrorType getType() { return type; }

    public enum ErrorType {
        AUDIO_CAPTURE,    // 音频采集错误
        PROCESSING,       // 处理错误
        RECOGNITION,      // 识别错误
        UNDERSTANDING,    // 理解错误
        EXECUTION         // 执行错误
    }
}