package com.example.design.build;

/**
 * TTS 引擎 SDK - 非建造者模式实现
 */
public class BasicTTSEngine {
    // 声音类型枚举
    public enum VoiceType {
        MALE_DEEP,       // 低沉男声
        FEMALE_SOFT,     // 温柔女声
        CHILD,           // 儿童声音
        ROBOTIC,         // 机器人声音
        CUSTOM           // 自定义声音
    }

    // 语言类型枚举
    public enum Language {
        ENGLISH_US,
        ENGLISH_UK,
        MANDARIN,
        CANTONESE,
        JAPANESE,
        KOREAN,
        SPANISH
    }

    // 配置参数
    private VoiceType voiceType;
    private float speed;
    private float pitch;
    private int volume;
    private Language language;
    private boolean async;
    private String customVoice;

    private boolean isPlaying = false;

    public BasicTTSEngine() {
        // 默认配置
        this.voiceType = VoiceType.FEMALE_SOFT;
        this.speed = 1.0f;
        this.pitch = 1.0f;
        this.volume = 80;
        this.language = Language.MANDARIN;
        this.async = false;
        this.customVoice = null;

        initializeEngine();
    }

    // 全参数构造函数 - 难以使用和维护
    public BasicTTSEngine(VoiceType voiceType, float speed, float pitch, int volume,
                          Language language, boolean async, String customVoice) {
        validateParams(speed, pitch, volume);

        this.voiceType = voiceType;
        this.speed = speed;
        this.pitch = pitch;
        this.volume = volume;
        this.language = language;
        this.async = async;
        this.customVoice = customVoice;

        initializeEngine();
    }

    private void validateParams(float speed, float pitch, int volume) {
        if (speed < 0.5f || speed > 2.0f) {
            throw new IllegalArgumentException("语速必须在0.5到2.0之间");
        }
        if (pitch < 0.5f || pitch > 1.5f) {
            throw new IllegalArgumentException("音调必须在0.5到1.5之间");
        }
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("音量必须在0到100之间");
        }
    }

    private void initializeEngine() {
        // 模拟初始化TTS引擎
        System.out.println("初始化TTS引擎...");
        System.out.println("配置: " + this);
    }

    // 多个setter方法
    public void setVoiceType(VoiceType voiceType) {
        this.voiceType = voiceType;
    }

    public void setSpeed(float speed) {
        validateParams(speed, this.pitch, this.volume);
        this.speed = speed;
    }

    public void setPitch(float pitch) {
        validateParams(this.speed, pitch, this.volume);
        this.pitch = pitch;
    }

    public void setVolume(int volume) {
        validateParams(this.speed, this.pitch, volume);
        this.volume = volume;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setCustomVoice(String customVoice) {
        this.customVoice = customVoice;
    }

    public void speak(String text) {
        if (isPlaying) {
            System.out.println("警告: TTS引擎正在播放中，忽略新请求");
            return;
        }

        isPlaying = true;

        if (async) {
            new Thread(() -> doSpeak(text)).start();
        } else {
            doSpeak(text);
        }
    }

    private void doSpeak(String text) {
        System.out.println("开始播报: " + text);
        System.out.println("使用配置: " + toString());

        // 模拟语音合成和播放
        try {
            // 根据语速计算播放时间 (每字符0.1秒 * 语速因子)
            long duration = (long) (text.length() * 100 / speed);
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("播报完成");
        isPlaying = false;
    }

    @Override
    public String toString() {
        return String.format("声音类型: %s, 语速: %.1f, 音调: %.1f, 音量: %d%%, 语言: %s, 异步: %b, 自定义: %s",
                voiceType, speed, pitch, volume, language, async,
                customVoice != null ? customVoice : "无");
    }
}