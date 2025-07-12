package com.example.design.chain.use;

class VoiceEnhancer extends AudioProcessor {
    @Override
    public String getProcessorName() {
        return "语音增强";
    }

    @Override
    protected AudioData preProcess(AudioData input, ProcessingContext context) {
        // 根据设备类型调整增强参数
        String deviceType = context.get("deviceType", String.class);
        String filter = "语音增强";

        if ("headset".equals(deviceType)) {
            filter = "耳机优化增强";
        } else if ("car".equals(deviceType)) {
            filter = "车载环境增强";
        }

        return input.applyFilter(filter);
    }

    @Override
    protected AudioData postProcess(AudioData input, ProcessingContext context) {
        // 后处理：添加元数据
        context.put("enhancementApplied", true);
        context.put("finalSNRBeforeCompression", input.getSNR());
        return input;
    }
}

