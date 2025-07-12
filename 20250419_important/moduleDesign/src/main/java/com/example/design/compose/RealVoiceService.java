package com.example.design.compose;

public class RealVoiceService implements IVoiceService {
    @Override
    public void processVoice(byte[] data) {
        System.out.println("Processing voice data...");
    }

    @Override
    public String synthesizeText(String text) {
        return "Synthesized: " + text;
    }
}

