package com.example.design.compose.factory;

import com.example.design.compose.adapter.ThirdPartyRecognizer;

public class ThirdPartyRecognizerAdapter implements VoiceProcessor {
    private final ThirdPartyRecognizer thirdPartyRecognizer;

    public ThirdPartyRecognizerAdapter(ThirdPartyRecognizer recognizer) {
        this.thirdPartyRecognizer = recognizer;
    }

    @Override
    public String process(String audio) {
        byte[] data = audio.getBytes();
        return thirdPartyRecognizer.recognize(data);
    }
}
