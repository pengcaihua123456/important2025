package com.example.design.compose;

import com.example.design.compose.processing.VoiceProcessingTemplate;
import com.example.design.compose.processing.VoiceRecognitionContext;

public class StandardVoiceProcessing extends VoiceProcessingTemplate {
    private final VoiceRecognitionContext recognitionContext = new VoiceRecognitionContext();

    @Override
    protected String recognize(byte[] audio) {
        return recognitionContext.executeRecognition(audio);
    }
}
