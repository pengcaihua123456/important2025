package com.example.design.compose.command;

import com.example.design.compose.core.VoiceCommand;

// 6. 责任链模式 - 语音命令处理
public abstract class CommandHandler {
    protected CommandHandler next;

    public void setNext(CommandHandler next) {
        this.next = next;
    }

    public abstract boolean handleCommand(VoiceCommand command);
}
