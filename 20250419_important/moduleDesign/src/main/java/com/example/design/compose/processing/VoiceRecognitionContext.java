package com.example.design.compose.processing;

public class VoiceRecognitionContext {
    private RecognitionStrategy strategy = new FastRecognition();

    public void setStrategy(RecognitionStrategy strategy) {
        this.strategy = strategy;
    }

    public String executeRecognition(byte[] audioData) {
        return strategy.recognize(audioData);
    }
}

