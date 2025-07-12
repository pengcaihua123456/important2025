package com.example.design.chain.use;

// ====================== 客户端使用示例 ======================
public class ProfessionalAudioProcessing {
    public static void main(String[] args) {
        // 场景1：标准语音处理
        System.out.println("==================== 场景1: 标准语音处理 ====================");
        processAudioScenario("办公室会议录音", "office", "voice", "built-in");

//        // 场景2：风噪环境中的车载处理
//        System.out.println("\n==================== 场景2: 风噪环境车载处理 ====================");
//        processAudioScenario("车载免提通话", "windy", "voice", "car");
//
//        // 场景3：高质量音频跳过处理
//        System.out.println("\n==================== 场景3: 高质量音频处理 ====================");
//        processHighQualityAudio("录音室高质量音频", "studio", "music", "studio");
    }

    private static void processAudioScenario(String description, String environment,
                                             String audioType, String deviceType) {
        System.out.println("处理: " + description);
        System.out.println("环境: " + environment + " | 音频类型: " + audioType + " | 设备: " + deviceType);

        // 1. 创建处理链
        AudioProcessor chain = new AudioProcessingChainBuilder()
                .addProcessor(new NoiseDetectionProcessor())
                .addProcessor(new WindNoiseSuppressor()) // 根据环境条件可能跳过
                .addProcessor(new EchoCancellationProcessor())
                .addProcessor(new SpectralNoiseReducer())
                .addProcessor(new VoiceEnhancer())
                .addProcessor(new DynamicRangeCompressor())
                .build();

        // 2. 准备上下文
        ProcessingContext context = new ProcessingContext();
        context.put("environment", environment);
        context.put("audioType", audioType);
        context.put("deviceType", deviceType);

        // 3. 执行处理
        AudioData rawAudio = new AudioData("原始音频数据");
        System.out.println("开始处理: " + rawAudio);
        AudioData result = chain.process(rawAudio, context);

        // 4. 输出结果
        System.out.println("\n处理结果: " + result);
        if (context.shouldTerminate()) {
            System.out.println("! 处理被中断: " + context.getTerminationReason());
        }
        System.out.println("最终信噪比: " + context.get("finalSNR", Double.class) + " dB");
        System.out.println("总提升: " + context.get("totalImprovement", Double.class) + " dB\n");
    }

    private static void processHighQualityAudio(String description, String environment,
                                                String audioType, String deviceType) {
        System.out.println("处理: " + description);

        // 创建处理链
        AudioProcessor chain = new AudioProcessingChainBuilder()
                .addProcessor(new NoiseDetectionProcessor())
                .addProcessor(new EchoCancellationProcessor()) // 这里会检测到高质量音频并中断
                .addProcessor(new SpectralNoiseReducer())
                .build();

        // 准备上下文
        ProcessingContext context = new ProcessingContext();
        context.put("environment", environment);
        context.put("audioType", audioType);
        context.put("deviceType", deviceType);

        // 创建高质量音频（信噪比初始设为45dB）
        AudioData highQualityAudio = new AudioData("高质量音频数据") {
            @Override
            public double getSNR() {
                return 45.0; // 高质量音频
            }
        };

        System.out.println("开始处理: " + highQualityAudio);
        AudioData result = chain.process(highQualityAudio, context);

        System.out.println("\n处理结果: " + result);
        System.out.println("! 处理被中断: " + context.getTerminationReason());
        System.out.println("最终信噪比: " + highQualityAudio.getSNR() + " dB (保持不变)");
    }
}
