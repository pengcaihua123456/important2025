package com.example.design.proxy;

public class BasicVoiceRecognizer {
    private String currentLanguage = "zh-CN";

    // 核心业务方法 - 被辅助功能代码污染
    public String recognize(String audioData) {
        // 1. 网络检查（重复代码）
        if (!checkNetwork()) {
            throw new RuntimeException("网络不可用");
        }

        // 2. 日志记录（重复代码）
        logAction("recognize", audioData);

        // 3. 性能监控（重复代码）
        long startTime = System.currentTimeMillis();

        // 实际业务逻辑（被非业务代码包围）
        System.out.println("Processing audio data: " + audioData.substring(0, 10) + "...");
        String result = "识别结果: " + audioData.hashCode();

        // 4. 性能日志（重复代码）
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("METHOD PERFORMANCE: recognize executed in " + duration + "ms");

        return result;
    }

    // 另一个方法也需要重复相同的辅助代码
    public void setLanguage(String language) {
        // 网络检查（重复）
        if (!checkNetwork()) {
            throw new RuntimeException("网络不可用");
        }

        // 日志记录（重复）
        logAction("setLanguage", language);

        // 性能监控（重复）
        long startTime = System.currentTimeMillis();

        // 实际业务逻辑
        this.currentLanguage = language;
        System.out.println("Language set to: " + language);

        // 性能日志（重复）
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("METHOD PERFORMANCE: setLanguage executed in " + duration + "ms");
    }

    // 重复的辅助方法 - 网络检查
    private boolean checkNetwork() {
        // 模拟网络检查
        boolean isConnected = Math.random() > 0.2;
        System.out.println("NETWORK CHECK: " + (isConnected ? "Connected ✓" : "Disconnected ✗"));
        return isConnected;
    }

    // 重复的辅助方法 - 日志记录
    private void logAction(String methodName, Object arg) {
        System.out.println("METHOD CALL: " + methodName + " | ARGS: [" + arg + "]");
    }
}