package com.example.design.compose.core;

// 2. 建造者模式 - 语音命令构建
public class VoiceCommand {
    private final String action;
    private final String target;
    private final String params;
    private final String source;

    private VoiceCommand(Builder builder) {
        this.action = builder.action;
        this.target = builder.target;
        this.params = builder.params;
        this.source = builder.source;
    }

    public String getAction() { return action; }
    public String getTarget() { return target; }
    public String getParams() { return params; }
    public String getSource() { return source; }

    public static class Builder {
        private String action;
        private String target;
        private String params = "";
        private String source = "user";

        public Builder setAction(String action) {
            this.action = action;
            return this;
        }

        public Builder setTarget(String target) {
            this.target = target;
            return this;
        }

        public Builder setParams(String params) {
            this.params = params;
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public VoiceCommand build() {
            return new VoiceCommand(this);
        }
    }
}
