package com.example.design.faced;

public class NLUProcessor {
    public Intent parse(String text) {
        System.out.println("[NLUProcessor] 自然语言理解中...");

        // 简单的意图解析逻辑
        if (text.contains("打开") && text.contains("灯")) {
            String location = text.contains("客厅") ? "客厅" : "卧室";
            return new Intent("light_control", location, "on");
        } else if (text.contains("关闭")) {
            String location = text.contains("客厅") ? "客厅" : "卧室";
            return new Intent("light_control", location, "off");
        } else if (text.contains("温度")) {
            return new Intent("query", "temperature", "");
        }

        throw new VoiceException(
                VoiceException.ErrorType.UNDERSTANDING,
                "无法理解的指令: " + text
        );
    }
}