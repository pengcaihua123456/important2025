package com.example.design.faced;

// ===== 新增：意图表示类 =====
public class Intent {
    private final String action;
    private final String target;
    private final String value;

    public Intent(String action, String target, String value) {
        this.action = action;
        this.target = target;
        this.value = value;
    }

    public String getAction() { return action; }
    public String getTarget() { return target; }
    public String getValue() { return value; }

    @Override
    public String toString() {
        return action + ":" + target + "=" + value;
    }
}