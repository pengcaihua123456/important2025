package com.example.design.chain.use;

class EchoCancellationProcessor extends AudioProcessor {
    @Override
    public String getProcessorName() {
        return "回声消除";
    }

    @Override
    protected AudioData preProcess(AudioData input, ProcessingContext context) {
        AudioData processed = input.applyFilter("回声消除");

        // 检查是否高质量音频（跳过后续处理）
        if (processed.getSNR() > 40.0) {
            context.terminate("高质量音频跳过后续处理");
            context.log("检测到高质量音频 (SNR: " + processed.getSNR() + " dB)，跳过后续处理");
        }

        return processed;
    }

    @Override
    protected AudioData postProcess(AudioData input, ProcessingContext context) {
        // 后处理：轻微增强处理后的音频
        if (input.getSNR() > 25.0) {
            context.log("应用回声消除后增强");
            return input.applyFilter("轻微增强");
        }
        return input;
    }
}
