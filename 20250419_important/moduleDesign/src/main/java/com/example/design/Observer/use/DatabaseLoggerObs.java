package com.example.design.Observer.use;

import android.content.Context;

import com.example.design.Observer.DatabaseEngine;

// 数据库日志观察者
public class DatabaseLoggerObs implements VoiceObserverObs {
    private final DatabaseEngine dbEngine; // 未改名

    public DatabaseLoggerObs(Context context) {
        this.dbEngine = new DatabaseEngine(context);
    }

    @Override
    public void onVoiceCommandReceived(String command) {
        dbEngine.logVoiceCommand(command);
    }

    public void closeDatabase() {
        dbEngine.close();
    }
}
