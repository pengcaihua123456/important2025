package com.example.design.chain.use;

class SpectralNoiseReducer extends AudioProcessor {
    @Override
    public String getProcessorName() {
        return "频谱降噪";
    }

    @Override
    protected AudioData preProcess(AudioData input, ProcessingContext context) {
        // 检测环境类型
        String environment = context.get("environment", String.class);
        if ("windy".equals(environment)) {
            context.log("风噪环境，应用强化降噪");
            return input.applyFilter("强化频谱降噪");
        }
        return input.applyFilter("频谱降噪");
    }

    @Override
    protected AudioData postProcess(AudioData input, ProcessingContext context) {
        // 后处理：分析降噪效果
        double currentSNR = input.getSNR();
        double originalSNR = context.get("originalSNR", Double.class);

        if (currentSNR - originalSNR < 15.0) {
            context.log("降噪效果不足，应用补偿增强");
            return input.applyFilter("补偿增强");
        }
        return input;
    }
}
