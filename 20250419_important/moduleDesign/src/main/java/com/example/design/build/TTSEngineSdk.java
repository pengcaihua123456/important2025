package com.example.design.build;

import java.util.HashMap;
import java.util.Map;

/**
 * TTS 引擎 SDK - 建造者模式实现
 */
public class TTSEngineSdk {

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
        ENGLISH_US("en-US", "美式英语"),
        ENGLISH_UK("en-UK", "英式英语"),
        MANDARIN("zh-CN", "普通话"),
        CANTONESE("zh-HK", "粤语"),
        JAPANESE("ja-JP", "日语"),
        KOREAN("ko-KR", "韩语"),
        SPANISH("es-ES", "西班牙语");

        private final String code;
        private final String displayName;

        Language(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 情感参数枚举
    public enum Emotion {
        NEUTRAL,    // 中性
        HAPPY,      // 高兴
        SAD,        // 悲伤
        ANGRY,      // 愤怒
        SURPRISED,  // 惊讶
        CALM,       // 平静
        EXCITED     // 兴奋
    }

    // 音频格式枚举
    public enum AudioFormat {
        PCM_16BIT("audio/pcm;bit=16", "16-bit PCM"),
        MP3("audio/mpeg", "MP3"),
        AAC("audio/aac", "AAC"),
        OGG_VORBIS("audio/ogg;codec=vorbis", "OGG Vorbis"),
        WAV("audio/wav", "WAV");

        private final String mimeType;
        private final String displayName;

        AudioFormat(String mimeType, String displayName) {
            this.mimeType = mimeType;
            this.displayName = displayName;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 语音风格枚举
    public enum Style {
        NEWS_READING,      // 新闻播报
        STORY_TELLING,     // 故事讲述
        CONVERSATIONAL,    // 对话风格
        COMMANDING,        // 命令风格
        POETIC,            // 诗歌朗诵
        BUSINESS,          // 商务风格
        INFORMAL           // 非正式风格
    }

    // 特殊效果类
    public static class Effects {
        private final float echoLevel;         // 回声效果级别 (0.0-1.0)
        private final float reverbLevel;       // 混响效果级别 (0.0-1.0)
        private final Map<String, Float> eqSettings; // 均衡器设置

        private Effects(Builder builder) {
            this.echoLevel = builder.echoLevel;
            this.reverbLevel = builder.reverbLevel;
            this.eqSettings = builder.eqSettings;
        }

        public static class Builder {
            private float echoLevel = 0.0f;
            private float reverbLevel = 0.0f;
            private Map<String, Float> eqSettings = new HashMap<>();

            public Builder setEchoLevel(float level) {
                if (level < 0 || level > 1.0f) {
                    throw new IllegalArgumentException("回声级别必须在0.0-1.0之间");
                }
                this.echoLevel = level;
                return this;
            }

            public Builder setReverbLevel(float level) {
                if (level < 0 || level > 1.0f) {
                    throw new IllegalArgumentException("混响级别必须在0.0-1.0之间");
                }
                this.reverbLevel = level;
                return this;
            }

            public Builder setEQSetting(String band, float level) {
                if (level < -12.0f || level > 12.0f) {
                    throw new IllegalArgumentException("EQ级别必须在-12.0到12.0之间");
                }
                this.eqSettings.put(band, level);
                return this;
            }

            public Effects build() {
                return new Effects(this);
            }
        }

        @Override
        public String toString() {
            return String.format("回声: %.1f, 混响: %.1f, EQ: %s",
                    echoLevel, reverbLevel, eqSettings.toString());
        }
    }

    // 配置参数类
    public static class Config {
        final VoiceType voiceType;
        final float speed;            // 语速 (0.5-2.0)
        final float pitch;            // 音调 (0.5-1.5)
        final int volume;             // 音量 (0-100)
        final Language language;
        final boolean async;          // 是否异步播放
        final String customVoice;     // 自定义声音路径
        final Emotion emotion;        // 情感参数
        final AudioFormat audioFormat; // 音频格式
        final Style style;            // 语音风格
        final Effects effects;        // 特殊效果

        private Config(Builder builder) {
            this.voiceType = builder.voiceType;
            this.speed = builder.speed;
            this.pitch = builder.pitch;
            this.volume = builder.volume;
            this.language = builder.language;
            this.async = builder.async;
            this.customVoice = builder.customVoice;
            this.emotion = builder.emotion;
            this.audioFormat = builder.audioFormat;
            this.style = builder.style;
            this.effects = builder.effects;
        }
    }

    // 建造者类
    public static class Builder {
        // 默认配置
        private VoiceType voiceType = VoiceType.FEMALE_SOFT;
        private float speed = 1.0f;               // 正常语速
        private float pitch = 1.0f;               // 正常音调
        private int volume = 80;                  // 80% 音量
        private Language language = Language.MANDARIN;
        private boolean async = false;            // 默认同步
        private String customVoice = null;        // 无自定义
        private Emotion emotion = Emotion.NEUTRAL; // 中性情感
        private AudioFormat audioFormat = AudioFormat.MP3; // 默认MP3
        private Style style = Style.CONVERSATIONAL; // 对话风格
        private Effects effects = new Effects.Builder().build(); // 默认无效果

        public Builder setVoiceType(VoiceType voiceType) {
            this.voiceType = voiceType;
            return this;
        }

        public Builder setSpeed(float speed) {
            if (speed < 0.5f || speed > 2.0f) {
                throw new IllegalArgumentException("语速必须在0.5到2.0之间");
            }
            this.speed = speed;
            return this;
        }

        public Builder setPitch(float pitch) {
            if (pitch < 0.5f || pitch > 1.5f) {
                throw new IllegalArgumentException("音调必须在0.5到1.5之间");
            }
            this.pitch = pitch;
            return this;
        }

        public Builder setVolume(int volume) {
            if (volume < 0 || volume > 100) {
                throw new IllegalArgumentException("音量必须在0到100之间");
            }
            this.volume = volume;
            return this;
        }

        public Builder setLanguage(Language language) {
            this.language = language;
            return this;
        }

        public Builder setAsync(boolean async) {
            this.async = async;
            return this;
        }

        public Builder setCustomVoice(String voicePath) {
            this.customVoice = voicePath;
            return this;
        }

        public Builder setEmotion(Emotion emotion) {
            this.emotion = emotion;
            return this;
        }

        public Builder setAudioFormat(AudioFormat format) {
            this.audioFormat = format;
            return this;
        }

        public Builder setStyle(Style style) {
            this.style = style;
            return this;
        }

        public Builder setEffects(Effects effects) {
            this.effects = effects;
            return this;
        }

        public Builder setEffects(Effects.Builder effectsBuilder) {
            this.effects = effectsBuilder.build();
            return this;
        }

        public TTSEngineSdk build() {
            // 验证参数依赖关系
            if (customVoice != null && voiceType != VoiceType.CUSTOM) {
                throw new IllegalStateException("自定义声音需要设置voiceType为CUSTOM");
            }

            return new TTSEngineSdk(new Config(this));
        }
    }

    private final Config config;
    private boolean isPlaying = false;

    private TTSEngineSdk(Config config) {
        this.config = config;
        initializeEngine();
    }

    private void initializeEngine() {
        System.out.println("初始化TTS引擎...");
        System.out.println("配置: " + configToString());
    }

    public void speak(String text) {
        if (isPlaying) {
            System.out.println("警告: TTS引擎正在播放中，忽略新请求");
            return;
        }

        isPlaying = true;

        if (config.async) {
            new Thread(() -> doSpeak(text)).start();
        } else {
            doSpeak(text);
        }
    }

    public byte[] synthesize(String text) {
        System.out.println("合成音频: " + text);
        System.out.println("使用配置: " + configToString());

        // 模拟音频合成
        int audioLength = (int) (text.length() * 100 / config.speed);
        return new byte[audioLength];
    }

    private void doSpeak(String text) {
        System.out.println("开始播报: " + text);
        System.out.println("使用配置: " + configToString());

        // 模拟语音合成和播放
        try {
            // 根据语速计算播放时间 (每字符0.1秒 * 语速因子)
            long duration = (long) (text.length() * 100 / config.speed);
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("播报完成");
        isPlaying = false;
    }

    public String configToString() {
        return String.format(
                "声音类型: %s, 语速: %.1f, 音调: %.1f, 音量: %d%%, 语言: %s, 异步: %b\n" +
                        "自定义: %s, 情感: %s, 格式: %s, 风格: %s\n" +
                        "效果: %s",
                config.voiceType,
                config.speed,
                config.pitch,
                config.volume,
                config.language.getDisplayName(),
                config.async,
                config.customVoice != null ? config.customVoice : "无",
                config.emotion,
                config.audioFormat.getDisplayName(),
                config.style,
                config.effects
        );
    }

    public static Builder builder() {
        return new Builder();
    }
}