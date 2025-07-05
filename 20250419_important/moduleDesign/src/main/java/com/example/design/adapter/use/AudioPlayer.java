package com.example.design.adapter.use;

// 1. 定义统一播放接口 (Target)
public interface AudioPlayer {
    void play(String text);
    void pause();
    void stop();
    void release();
}
