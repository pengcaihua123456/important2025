package com.example.design.Observer.use;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// 2. 被观察者（主题）
public class VoiceRecognizerObs {
    private final List<VoiceObserverObs> observers = new ArrayList<>();

    public void registerObserver(VoiceObserverObs observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unregisterObserver(VoiceObserverObs observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String command) {
        for (VoiceObserverObs observer : observers) {
            observer.onVoiceCommandReceived(command);
        }
    }

    // 语音识别回调
    public void onVoiceCommandReceived(String command) {
        Log.i("VoiceRecognizerObs", "识别命令: " + command);
        notifyObservers(command);
    }
}
