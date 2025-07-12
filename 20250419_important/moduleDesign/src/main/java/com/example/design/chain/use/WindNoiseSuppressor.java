package com.example.design.chain.use;

class WindNoiseSuppressor extends AudioProcessor {
    @Override
    public String getProcessorName() {
        return "风噪抑制";
    }

    @Override
    protected AudioData preProcess(AudioData input, ProcessingContext context) {
        // 只有在风噪环境中才处理
        if ("windy".equals(context.get("environment", String.class))) {
            context.log("检测到风噪环境，应用风噪抑制");
            return input.applyFilter("风噪抑制");
        }
        context.log("无风噪环境，跳过处理");
        return input;
    }
}
