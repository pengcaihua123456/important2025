package com.example.design.compose.command;

import com.example.design.compose.CommandParser;

import java.util.HashMap;
import java.util.Map;

// 10. 享元模式 - 语音命令解析器复用
public class CommandParserFactory {
    private static final Map<String, CommandParser> parserCache = new HashMap<>();

    public static CommandParser getParser(String language) {
        if (!parserCache.containsKey(language)) {
            parserCache.put(language, new CommandParser(language));
        }
        return parserCache.get(language);
    }
}

