package com.example.design.chain.use;

// ====================== 具体处理器实现 ======================
class NoiseDetectionProcessor extends AudioProcessor {
    @Override
    public String getProcessorName() {
        return "环境噪声检测";
    }

    @Override
    protected AudioData preProcess(AudioData input, ProcessingContext context) {
        context.put("originalSNR", input.getSNR());
        return input.applyFilter("环境噪声检测");
    }

    @Override
    protected AudioData postProcess(AudioData input, ProcessingContext context) {
        double originalSNR = context.get("originalSNR", Double.class);
        double improvement = input.getSNR() - originalSNR;
        context.log("噪声检测完成，信噪比提升: " + String.format("%.1f", improvement) + " dB");
        return input;
    }
}
