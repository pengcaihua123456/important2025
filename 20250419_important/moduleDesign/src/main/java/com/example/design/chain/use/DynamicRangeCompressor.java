package com.example.design.chain.use;

class DynamicRangeCompressor extends AudioProcessor {
    @Override
    public String getProcessorName() {
        return "动态范围压缩";
    }

    @Override
    protected AudioData preProcess(AudioData input, ProcessingContext context) {
        // 根据音乐/语音类型调整压缩强度
        String audioType = context.get("audioType", String.class);
        String filter = "动态范围压缩";

        if ("music".equals(audioType)) {
            filter = "音乐优化压缩";
        } else if ("voice".equals(audioType)) {
            filter = "语音优化压缩";
        }

        return input.applyFilter(filter);
    }

    @Override
    protected AudioData postProcess(AudioData input, ProcessingContext context) {
        // 收集最终指标
        double originalSNR = context.get("originalSNR", Double.class);
        double finalSNR = input.getSNR();
        double improvement = finalSNR - originalSNR;

        context.log("处理完成！总信噪比提升: " + String.format("%.1f", improvement) + " dB");
        context.put("finalSNR", finalSNR);
        context.put("totalImprovement", improvement);

        return input;
    }
}

