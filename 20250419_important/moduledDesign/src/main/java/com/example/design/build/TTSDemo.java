package com.example.design.build;

public class TTSDemo {

    public static void main(String[] args) {
        // 场景1: 儿童故事讲述
        TTSEngineSdk storyTeller = TTSEngineSdk.builder()
                .setVoiceType(TTSEngineSdk.VoiceType.CHILD)
                .setSpeed(0.9f)
                .setPitch(1.2f)
                .setEmotion(TTSEngineSdk.Emotion.EXCITED)
                .setStyle(TTSEngineSdk.Style.STORY_TELLING)
                .setEffects(new TTSEngineSdk.Effects.Builder()
                        .setReverbLevel(0.3f)
                        .build())
                .build();

        storyTeller.speak("很久很久以前，在一个遥远的王国里...");

        // 场景2: 新闻播报
        TTSEngineSdk newsReader = TTSEngineSdk.builder()
                .setVoiceType(TTSEngineSdk.VoiceType.MALE_DEEP)
                .setSpeed(1.1f)
                .setVolume(90)
                .setStyle(TTSEngineSdk.Style.NEWS_READING)
                .setAudioFormat(TTSEngineSdk.AudioFormat.PCM_16BIT)
                .build();

        newsReader.speak("今日头条：人工智能技术取得重大突破...");

        // 场景3: 机器人语音提示
        TTSEngineSdk robotVoice = TTSEngineSdk.builder()
                .setVoiceType(TTSEngineSdk.VoiceType.ROBOTIC)
                .setPitch(0.8f)
                .setEmotion(TTSEngineSdk.Emotion.CALM)
                .setEffects(new TTSEngineSdk.Effects.Builder()
                        .setEchoLevel(0.5f)
                        .setEQSetting("bass", 3.0f)
                        .setEQSetting("treble", 2.0f)
                        .build())
                .build();

        robotVoice.speak("系统启动完成。当前时间：" + new java.util.Date());

        // 场景4: 自定义情感语音合成
        TTSEngineSdk emotionEngine = TTSEngineSdk.builder()
                .setVoiceType(TTSEngineSdk.VoiceType.FEMALE_SOFT)
                .setEmotion(TTSEngineSdk.Emotion.SAD)
                .setStyle(TTSEngineSdk.Style.POETIC)
                .setLanguage(TTSEngineSdk.Language.JAPANESE)
                .setAudioFormat(TTSEngineSdk.AudioFormat.WAV)
                .build();

        byte[] audioData = emotionEngine.synthesize("春はあけぼの。やうやう白くなりゆく山際、少し明かりて、紫だちたる雲の細くたなびきたる。");
        System.out.println("合成音频长度: " + audioData.length + " 字节");

        // 场景5: 商务场景使用
        TTSEngineSdk businessVoice = TTSEngineSdk.builder()
                .setVoiceType(TTSEngineSdk.VoiceType.MALE_DEEP)
                .setSpeed(1.0f)
                .setVolume(85)
                .setStyle(TTSEngineSdk.Style.BUSINESS)
                .setLanguage(TTSEngineSdk.Language.ENGLISH_US)
                .build();

        businessVoice.speak("Good morning, ladies and gentlemen. Let's begin today's quarterly financial review.");

    }


    public static void mainBase(String[] args) {
        // 创建TTS实例
        BasicTTSEngine tts = new BasicTTSEngine();

        // 配置参数 - 需要多个setter调用
        tts.setVoiceType(BasicTTSEngine.VoiceType.CHILD);
        tts.setSpeed(1.2f);
        tts.setPitch(1.1f);
        tts.setVolume(90);
        tts.setLanguage(BasicTTSEngine.Language.ENGLISH_US);
        tts.setAsync(true);
        tts.setCustomVoice("/voices/custom_child.wav");

        // 使用TTS引擎
        tts.speak("Hello, welcome to our basic TTS engine demo!");

        // 创建另一个配置的TTS实例
        // 必须使用全参数构造函数 - 非常难用
        BasicTTSEngine fastTts;
        try {
            fastTts = new BasicTTSEngine(
                    BasicTTSEngine.VoiceType.FEMALE_SOFT,
                    1.8f,
                    1.0f,
                    100,
                    BasicTTSEngine.Language.MANDARIN,
                    false,
                    null
            );
        } catch (Exception e) {
            System.err.println("创建TTS失败: " + e.getMessage());
            return;
        }

        fastTts.speak("这是一个快速公告!");
    }

}