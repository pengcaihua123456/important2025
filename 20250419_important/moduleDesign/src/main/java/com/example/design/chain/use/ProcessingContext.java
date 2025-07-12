package com.example.design.chain.use;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ProcessingContext {
    private Map<String, Object> metadata = new HashMap<>();
    private boolean terminated = false;
    private String terminationReason;
    private Deque<String> callStack = new ArrayDeque<>();

    public void put(String key, Object value) {
        metadata.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(metadata.get(key));
    }

    public void terminate(String reason) {
        terminated = true;
        terminationReason = reason;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void pushProcessor(String processorName) {
        callStack.push(processorName);
        System.out.println("→ 进入 " + processorName);
    }

    public void popProcessor(String processorName) {
        String popped = callStack.pop();
        if (!popped.equals(processorName)) {
            System.out.println("⚠️ 调用栈错误! 期望弹出 " + processorName + " 但实际弹出 " + popped);
        }
        System.out.println("← 返回 " + processorName);
    }

    public void log(String message) {
        System.out.println("    │  [CONTEXT] " + message);
    }

    public boolean shouldTerminate() {
        return false;
    }
}

