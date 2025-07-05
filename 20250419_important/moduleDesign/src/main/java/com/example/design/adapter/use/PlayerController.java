package com.example.design.adapter.use;

// 4. 播放控制器（客户端）
public class PlayerController {
    private AudioPlayer currentPlayer;

    public void setPlayer(AudioPlayer player) {
        if (currentPlayer != null) {
            currentPlayer.release();
        }
        currentPlayer = player;
        System.out.println("播放器已切换");
    }

    public void playText(String text) {
        if (currentPlayer == null) {
            System.out.println("错误：未设置播放器");
            return;
        }
        currentPlayer.play(text);
    }

    public void pause() {
        if (currentPlayer != null) {
            currentPlayer.pause();
        }
    }

    public void stop() {
        if (currentPlayer != null) {
            currentPlayer.stop();
        }
    }

    public void release() {
        if (currentPlayer != null) {
            currentPlayer.release();
            currentPlayer = null;
        }
    }
}