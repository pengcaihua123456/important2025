package com.example.design.strategy;

public class VoiceAssistant {
    private final SpeechRecognitionContext recognitionContext = new SpeechRecognitionContext();

    public void onVoiceCommandReceived(byte[] audioData) {
        // 根据当前场景动态切换策略
        if (isMedicalContext()) {
            recognitionContext.setStrategy("medical");
        } else if (!NetworkUtils.isNetworkAvailable()) {
            recognitionContext.setStrategy("local");
        } else if (isHighAccuracyRequired()) {
            recognitionContext.setStrategy("cloud");
        }

        // 执行识别
        String text = recognitionContext.recognizeSpeech(audioData);
        processCommand(text);
    }

    private void processCommand(String text) {
        // 处理识别结果
        System.out.println("识别结果: " + text);
        System.out.println("使用策略: " + recognitionContext.getCurrentStrategyName());
    }

    // 示例辅助方法
    private boolean isMedicalContext() {
        // 判断是否在医疗场景
        return false;
    }

    private boolean isHighAccuracyRequired() {
        // 判断是否需要高精度
        return true;
    }



    private final SpeechRecognizerWithoutPattern recognizer = new SpeechRecognizerWithoutPattern();

    public void onVoiceCommandReceivedWithout(byte[] audioData) {
        // 根据当前场景设置模式
        if (isMedicalContext()) {
            recognizer.setMode(SpeechRecognizerWithoutPattern.MODE_MEDICAL);
        } else if (!NetworkUtils.isNetworkAvailable()) {
            recognizer.setMode(SpeechRecognizerWithoutPattern.MODE_LOCAL);
        } else if (isHighAccuracyRequired()) {
            recognizer.setMode(SpeechRecognizerWithoutPattern.MODE_CLOUD);
        }

        // 执行识别
        String text;
        try {
            text = recognizer.recognizeSpeech(audioData);
        } catch (Exception e) {
            // 错误处理...
            text = "识别失败";
        }

        processCommandWithout(text);
    }

    private void processCommandWithout(String text) {
        // 处理识别结果
        System.out.println("识别结果: " + text);
        System.out.println("使用模式: " + recognizer.getCurrentModeName());
    }




}