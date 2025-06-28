package com.example.design.strategy;

// 语音识别策略接口
public interface SpeechRecognitionStrategy {
    /**
     * 识别语音
     * @param audioData 音频数据
     * @return 识别结果文本
     */
    String recognize(byte[] audioData);

    /**
     * 获取策略名称
     */
    String getStrategyName();

    /**
     * 是否可用（如网络依赖）
     */
    boolean isAvailable();
}