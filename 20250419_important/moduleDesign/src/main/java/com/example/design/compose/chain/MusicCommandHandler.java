package com.example.design.compose.chain;

import com.example.design.compose.command.CommandHandler;
import com.example.design.compose.core.VoiceCommand;

public class MusicCommandHandler extends CommandHandler {
    @Override
    public boolean handleCommand(VoiceCommand command) {
        if ("PLAY".equals(command.getAction()) && "MUSIC".equals(command.getTarget())) {
            System.out.println("Playing music: " + command.getParams());
            return true;
        }
        return next != null && next.handleCommand(command);
    }
}
