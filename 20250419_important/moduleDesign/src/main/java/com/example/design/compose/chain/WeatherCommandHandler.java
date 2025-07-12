package com.example.design.compose.chain;

import com.example.design.compose.command.CommandHandler;
import com.example.design.compose.core.VoiceCommand;

public class WeatherCommandHandler extends CommandHandler {
    @Override
    public boolean handleCommand(VoiceCommand command) {
        if ("GET".equals(command.getAction()) && "WEATHER".equals(command.getTarget())) {
            System.out.println("Fetching weather for: " + command.getParams());
            return true;
        }
        return next != null && next.handleCommand(command);
    }
}

