package com.example.design.adapter.use;

// 2. 百度播放器适配器
public class BaiduPlayerAdapter implements AudioPlayer {
    // 模拟百度SDK
    public static class BaiduSdk {
        public void startPlay(String content) {
            System.out.println("[百度] 播放中: " + content);
        }

        public void pausePlay() {
            System.out.println("[百度] 已暂停");
        }

        public void stopPlay() {
            System.out.println("[百度] 已停止");
        }

        public void release() {
            System.out.println("[百度] 资源已释放");
        }
    }

    private final BaiduSdk baiduPlayer;

    public BaiduPlayerAdapter() {
        this.baiduPlayer = new BaiduSdk();
        System.out.println("百度播放器适配器初始化完成");
    }

    @Override
    public void play(String text) {
        // 适配器将统一接口转换为百度SDK特定调用
        baiduPlayer.startPlay(text);
    }

    @Override
    public void pause() {
        baiduPlayer.pausePlay();
    }

    @Override
    public void stop() {
        baiduPlayer.stopPlay();
    }

    @Override
    public void release() {
        baiduPlayer.release();
    }
}