package com.example.design.faced;

// ===== 外观类 =====
public class VoiceSystemFacade {
    private final AudioCapture capture;
    private final NoiseReducer reducer;
    private final VADetector vad;
    private final SpeechRecognizer recognizer;
    private final NLUProcessor nlu;
    private final CommandExecutor executor;
    private final TTSEngine tts;
    private final AudioPlayer player;

    public VoiceSystemFacade() {
        // 初始化所有子系统组件
        this.capture = new AudioCapture();
        this.reducer = new NoiseReducer();
        this.vad = new VADetector();
        this.recognizer = new SpeechRecognizer();
        this.nlu = new NLUProcessor();
        this.executor = new CommandExecutor();
        this.tts = new TTSEngine();
        this.player = new AudioPlayer();

        System.out.println("语音系统初始化完成");
    }

    // 核心简化接口
    public void processVoiceCommand(int durationMs) {
        try {
            // 1. 音频采集与处理
            byte[] processedAudio = processAudio(durationMs);

            // 2. 语音识别与理解
            Intent intent = recognizeAndParse(processedAudio);

            // 3. 执行命令
            executeCommand(intent);

            // 4. 语音反馈
            provideFeedback("已执行命令: " + intent.getAction());

        } catch (VoiceException e) {
            handleError(e);
        }
    }

    // 语音识别专用接口
    public String recognizeSpeech(int durationMs) {
        byte[] audio = processAudio(durationMs);
        return recognizer.recognize(audio);
    }

    // 语音播报接口
    public void speak(String text) {
        byte[] audio = tts.synthesize(text);
        player.play(audio);
    }

    // ===== 私有方法封装实现细节 =====
    private byte[] processAudio(int durationMs) {
        byte[] raw = capture.record(durationMs);
        byte[] clean = reducer.process(raw);
        return vad.removeSilence(clean);
    }

    private Intent recognizeAndParse(byte[] audio) {
        String text = recognizer.recognize(audio);
        return nlu.parse(text);
    }

    private void executeCommand(Intent intent) {
        executor.execute(intent);
    }

    private void provideFeedback(String message) {
        byte[] audio = tts.synthesize(message);
        player.play(audio);
    }

    private void handleError(VoiceException e) {
        String errorMsg = "语音指令处理失败: " + e.getMessage();
        System.err.println(errorMsg);
        speak(errorMsg);
    }
}