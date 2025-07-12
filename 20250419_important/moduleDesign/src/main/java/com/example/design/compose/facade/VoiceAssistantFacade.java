package com.example.design.compose.facade;

import com.example.design.compose.CommandParser;
import com.example.design.compose.IVoiceService;
import com.example.design.compose.observer.RecognitionListener;
import com.example.design.compose.StandardVoiceProcessing;
import com.example.design.compose.processing.VoiceProcessingTemplate;
import com.example.design.compose.processing.VoiceRecognitionSubject;
import com.example.design.compose.VoiceServiceProxy;
import com.example.design.compose.chain.MusicCommandHandler;
import com.example.design.compose.chain.WeatherCommandHandler;
import com.example.design.compose.command.CommandHandler;
import com.example.design.compose.command.CommandParserFactory;
import com.example.design.compose.command.SystemCommandHandler;
import com.example.design.compose.core.VoiceCommand;
import com.example.design.compose.core.VoiceConfigManager;

// 11. 外观模式 - 语音系统统一接口
public class VoiceAssistantFacade {
    private final VoiceRecognitionSubject recognitionSubject = new VoiceRecognitionSubject();
    private final VoiceProcessingTemplate processingTemplate = new StandardVoiceProcessing();
    private final CommandHandler commandHandlerChain;

    private final IVoiceService voiceService;

    public VoiceAssistantFacade() {
        // 创建责任链
        CommandHandler musicHandler = new MusicCommandHandler();
        CommandHandler weatherHandler = new WeatherCommandHandler();
        CommandHandler systemHandler = new SystemCommandHandler();

        musicHandler.setNext(weatherHandler);
        weatherHandler.setNext(systemHandler);

        this.commandHandlerChain = musicHandler;

        // 创建带代理的语音服务
        this.voiceService = VoiceServiceProxy.createProxy();
    }

    public void startListening() {
        System.out.println("Listening for voice input...");
        // 模拟语音输入
        byte[] audioData = "播放周杰伦的歌".getBytes();
        processAudio(audioData);
    }

    public void processAudio(byte[] audioData) {
        new Thread(() -> {
            try {
                String result = processingTemplate.process(audioData);
                recognitionSubject.notifyResult(result);
            } catch (Exception e) {
                recognitionSubject.notifyError("Processing error: " + e.getMessage());
            }
        }).start();
    }

    public void addRecognitionListener(RecognitionListener listener) {
        recognitionSubject.addListener(listener);
    }

    // 处理命令的方法（被模板方法调用）
    public String handleCommandInternal(String text) {
        VoiceConfigManager config = VoiceConfigManager.getInstance();
        CommandParser parser = CommandParserFactory.getParser(config.getLanguage());
        VoiceCommand command = parser.parse(text);

        if (commandHandlerChain.handleCommand(command)) {
            return "Command executed: " + command.getAction() + " " + command.getTarget();
        }
        return "Unknown command: " + text;
    }

    // 语音合成
    public String synthesize(String text) {
        return voiceService.synthesizeText(text);
    }
}

