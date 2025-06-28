package com.example.design.faced;

public class CommandExecutor {
    public void execute(Intent intent) {
        System.out.println("[CommandExecutor] 执行命令: " + intent);

        // 模拟10%概率执行失败
        if (Math.random() > 0.9) {
            throw new VoiceException(
                    VoiceException.ErrorType.EXECUTION,
                    "设备未响应"
            );
        }

        // 实际设备控制逻辑...
    }
}