package com.example.design.compose;

import com.example.design.compose.core.VoiceCommand;

public class CommandParser {
    private final String language;

    public CommandParser(String language) {
        this.language = language;
        // 初始化解析器（模拟耗时操作）
        System.out.println("Initializing parser for: " + language);
    }

    public VoiceCommand parse(String text) {
        // 简化的解析逻辑
        if (text.contains("播放")) {
            return new VoiceCommand.Builder()
                    .setAction("PLAY")
                    .setTarget("MUSIC")
                    .setParams(text.replace("播放", "").trim())
                    .build();
        } else if (text.contains("天气")) {
            return new VoiceCommand.Builder()
                    .setAction("GET")
                    .setTarget("WEATHER")
                    .setParams(text.replace("天气", "").trim())
                    .build();
        }
        return new VoiceCommand.Builder()
                .setAction("UNKNOWN")
                .setTarget("UNKNOWN")
                .setParams(text)
                .build();
    }
}
