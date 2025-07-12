package com.example.design.compose.command;

import com.example.design.compose.core.VoiceCommand;
import com.example.design.compose.core.VoiceConfigManager;

public class SystemCommandHandler extends CommandHandler {
    @Override
    public boolean handleCommand(VoiceCommand command) {
        if ("SET".equals(command.getAction()) && "VOLUME".equals(command.getTarget())) {
            try {
                int volume = Integer.parseInt(command.getParams());
                VoiceConfigManager.getInstance().setVolume(volume);
                System.out.println("Volume set to: " + volume);
                return true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid volume value");
            }
        }
        return next != null && next.handleCommand(command);
    }
}
