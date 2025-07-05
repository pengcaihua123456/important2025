package com.example.design.Observer.use;

import android.util.Log;

// 云端同步观察者
public class CloudSyncerObs implements VoiceObserverObs {
    @Override
    public void onVoiceCommandReceived(String command) {
        new Thread(() -> {
            Log.i("CloudSyncerObs", "同步命令: " + command);
            // 实际网络请求代码...
        }).start();
    }
}
