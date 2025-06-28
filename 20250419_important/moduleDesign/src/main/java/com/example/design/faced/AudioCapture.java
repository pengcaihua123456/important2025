package com.example.design.faced;

// ===== 子系统组件 =====
public class AudioCapture {
    public byte[] record(int durationMs) {
        System.out.println("[AudioCapture] 录制音频中...时长: " + durationMs + "ms");
        // 模拟10%概率出现采集失败
        if (Math.random() > 0.9) {
            throw new VoiceException(
                    VoiceException.ErrorType.AUDIO_CAPTURE,
                    "麦克风未就绪"
            );
        }
        return new byte[1024]; // 模拟音频数据
    }
}
