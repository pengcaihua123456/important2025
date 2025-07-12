package com.example.design.compose.core;

import com.example.design.compose.factory.VoiceProcessor;

public class SpeechProcessorFactory {
    public static VoiceProcessor createProcessor(String type) {
        VoiceConfigManager config = VoiceConfigManager.getInstance();

        if ("RECOGNITION".equals(type)) {
            if (config.isOfflineMode()) {
                return new OfflineSpeechRecognizer();
            }
            return new OnlineSpeechRecognizer();
        } else if ("SYNTHESIS".equals(type)) {
            if (config.isOfflineMode()) {
                return new OfflineSpeechSynthesizer();
            }
            return new OnlineSpeechSynthesizer();
        }
        throw new IllegalArgumentException("Invalid processor type");
    }
}

class OnlineSpeechRecognizer implements VoiceProcessor {
    @Override
    public String process(String audio) {
        return "Online Recognition: " + audio;
    }
}

class OfflineSpeechRecognizer implements VoiceProcessor {
    @Override
    public String process(String audio) {
        return "Offline Recognition: " + audio;
    }
}

class OnlineSpeechSynthesizer implements VoiceProcessor {
    @Override
    public String process(String text) {
        return "Synthesized: " + text;
    }
}

class OfflineSpeechSynthesizer implements VoiceProcessor {
    @Override
    public String process(String text) {
        return "Offline Synthesized: " + text;
    }
}
