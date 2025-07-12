package com.example.design.chain;

public class NonChainAudioProcessing {

    // 处理上下文
    static class ProcessingContext {
        int depth = 0; // 当前调用深度
        boolean terminated = false;
        String terminationReason;

        void enterProcessor(String name) {
            System.out.println(indent(depth) + "→ 进入 " + name);
            depth++;
        }

        void exitProcessor(String name) {
            depth--;
            System.out.println(indent(depth) + "← 返回 " + name);
        }

        void terminate(String reason) {
            terminated = true;
            terminationReason = reason;
        }

        private String indent(int level) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < level; i++) {
                sb.append("  ");  // 每次添加两个空格
            }
            return sb.toString();
        }
    }

    // 音频数据结构
    static class AudioData {
        double snr;
        StringBuilder history = new StringBuilder();

        AudioData(double snr) {
            this.snr = snr;
        }

        void applyFilter(String name, double improvement) {
            snr += improvement;
            history.append(indent(context.depth)).append("├─ ")
                    .append(name).append(" (+").append(improvement).append(" dB)\n");
        }

        private String indent(int level) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < level; i++) {
                sb.append("  ");  // 每次添加两个空格
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "音频数据 [SNR: " + snr + " dB]\n处理历史:\n" + history.toString();
        }
    }

    // 处理上下文实例
    private static ProcessingContext context = new ProcessingContext();

    public static void main(String[] args) {
        System.out.println("========= 专业语音降噪处理 =========");
        AudioData audio = new AudioData(10.0); // 初始信噪比10dB

        // 非责任链的递归处理流程
        audio = processAudio(audio);

        System.out.println("\n最终结果:");
        System.out.println(audio);

        if (context.terminated) {
            System.out.println("处理中断原因: " + context.terminationReason);
        }
    }

    // 核心递归处理函数
    private static AudioData processAudio(AudioData input) {
        // 1. 环境噪声检测
        context.enterProcessor("环境噪声检测");
        input = processNoiseDetection(input);
        if (context.terminated) {
            context.exitProcessor("环境噪声检测");
            return input;
        }

        // 2. 回声消除
        context.enterProcessor("回声消除");
        input = processEchoCancellation(input);
        if (context.terminated) {
            context.exitProcessor("回声消除");
            context.exitProcessor("环境噪声检测");
            return input;
        }

        // 3. 频谱降噪
        context.enterProcessor("频谱降噪");
        input = processSpectralReduction(input);
        if (context.terminated) {
            context.exitProcessor("频谱降噪");
            context.exitProcessor("回声消除");
            context.exitProcessor("环境噪声检测");
            return input;
        }

        // 4. 语音增强
        context.enterProcessor("语音增强");
        input = processVoiceEnhancement(input);

        // 5. 动态范围压缩
        context.enterProcessor("动态范围压缩");
        input = processDynamicCompression(input);
        context.exitProcessor("动态范围压缩");

        // 递归返回过程
        context.exitProcessor("语音增强");
        context.exitProcessor("频谱降噪");
        context.exitProcessor("回声消除");
        context.exitProcessor("环境噪声检测");

        return input;
    }

    // 具体处理器实现
    private static AudioData processNoiseDetection(AudioData input) {
        input.applyFilter("环境噪声检测", 5.0);

        // 简单降噪后质量检查
        if (input.snr > 25.0) {
            context.terminate("高质量音频跳过后续处理");
        }

        return input;
    }

    private static AudioData processEchoCancellation(AudioData input) {
        input.applyFilter("回声消除", 8.0);

        // 在回声消除阶段的中断检查
        if (input.snr > 35.0) {
            context.terminate("回声消除后音频质量达标");
        }

        return input;
    }

    private static AudioData processSpectralReduction(AudioData input) {
        input.applyFilter("频谱降噪", 12.0);
        return input;
    }

    private static AudioData processVoiceEnhancement(AudioData input) {
        input.applyFilter("语音增强", 7.0);
        return input;
    }

    private static AudioData processDynamicCompression(AudioData input) {
        input.applyFilter("动态范围压缩", 3.0);
        return input;
    }
}
