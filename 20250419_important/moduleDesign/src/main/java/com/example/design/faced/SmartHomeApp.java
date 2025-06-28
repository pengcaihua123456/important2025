package com.example.design.faced;

// ===== 客户端调用 =====
public class SmartHomeApp {
    public void processVoiceCommand() {
        // 1. 音频采集
        AudioCapture capture = new AudioCapture();
        byte[] rawAudio = capture.record(3000);

        // 2. 音频预处理
        NoiseReducer reducer = new NoiseReducer();
        byte[] cleanAudio = reducer.process(rawAudio);

        VADetector vad = new VADetector();
        byte[] finalAudio = vad.removeSilence(cleanAudio);

        // 3. 语音识别
        SpeechRecognizer recognizer = new SpeechRecognizer();
        String text = recognizer.recognize(finalAudio);

        // 4. 语义理解
        NLUProcessor nlu = new NLUProcessor();
        Intent intent = nlu.parse(text);

        // 5. 执行命令
        CommandExecutor executor = new CommandExecutor();
        executor.execute(intent);

        // 6. 语音反馈
        TTSEngine tts = new TTSEngine();
        byte[] responseAudio = tts.synthesize("已为您打开客厅的灯");

        AudioPlayer player = new AudioPlayer();
        player.play(responseAudio);
    }

    public static void processVoiceCommandByFaced() {
        // 单行代码完成整个语音流程
        voiceSystem.processVoiceCommand(3000);
    }

    private static VoiceSystemFacade voiceSystem;

    public static void announceTime() {
        voiceSystem.speak("当前时间是 ");
    }

    public static void main(String[] args) {

        System.out.println("------------------main-------------------");

        SmartHomeApp app = new SmartHomeApp();
        app.processVoiceCommand();

        voiceSystem = new VoiceSystemFacade();
        announceTime();
        processVoiceCommandByFaced();
    }
}