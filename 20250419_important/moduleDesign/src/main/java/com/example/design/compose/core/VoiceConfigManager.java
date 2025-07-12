package com.example.design.compose.core;

// 1. 单例模式 - 全局配置管理
public class VoiceConfigManager {
    private static volatile VoiceConfigManager instance;
    private String language = "zh-CN";
    private int volume = 80;
    private boolean useOfflineMode = false;

    private VoiceConfigManager() {}

    public static VoiceConfigManager getInstance() {
        if (instance == null) {
            synchronized (VoiceConfigManager.class) {
                if (instance == null) {
                    instance = new VoiceConfigManager();
                }
            }
        }
        return instance;
    }

    public void setLanguage(String lang) { this.language = lang; }
    public String getLanguage() { return language; }
    public void setVolume(int volume) { this.volume = volume; }
    public int getVolume() { return volume; }
    public void setUseOfflineMode(boolean use) { this.useOfflineMode = use; }
    public boolean isOfflineMode() { return useOfflineMode; }
}
