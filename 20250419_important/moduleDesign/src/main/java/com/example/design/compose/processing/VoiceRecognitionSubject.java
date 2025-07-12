package com.example.design.compose.processing;

import com.example.design.compose.observer.RecognitionListener;

import java.util.ArrayList;
import java.util.List;

public class VoiceRecognitionSubject {
    private final List<RecognitionListener> listeners = new ArrayList<>();

    public void addListener(RecognitionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RecognitionListener listener) {
        listeners.remove(listener);
    }

    public void notifyResult(String result) {
        for (RecognitionListener listener : listeners) {
            listener.onRecognitionResult(result);
        }
    }

    public void notifyError(String error) {
        for (RecognitionListener listener : listeners) {
            listener.onError(error);
        }
    }
}
