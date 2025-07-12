package com.example.design.compose.processing;

public class AccurateRecognition implements RecognitionStrategy {
    @Override
    public String recognize(byte[] audioData) {
        return "Accurate result: " + new String(audioData);
    }
}
