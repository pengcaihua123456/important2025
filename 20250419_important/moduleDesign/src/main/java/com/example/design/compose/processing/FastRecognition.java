package com.example.design.compose.processing;

public class FastRecognition implements RecognitionStrategy {
    @Override
    public String recognize(byte[] audioData) {
        return "Fast result: " + new String(audioData).substring(0, 10);
    }
}
