package com.example.design.chain.use;

// ====================== 抽象处理器 ======================
abstract class AudioProcessor {
    protected AudioProcessor next;

    public void setNext(AudioProcessor next) {
        this.next = next;
    }

    public AudioProcessor getNext() {
        return next;
    }

    public AudioData process(AudioData input, ProcessingContext context) {
        System.out.println("→ 进入 " + getProcessorName());

        // 1. 预处理
        AudioData preprocessed = preProcess(input, context);
        context.log("预处理完成: " + preprocessed);

        AudioData result = preprocessed;

        // 2. 传递给下一个处理器（如果存在且未中断）
        if (next != null && !context.shouldTerminate()) {
            context.log("传递给下一处理器: " + next.getProcessorName());
            result = next.process(preprocessed, context);
        }

        // 3. 后处理（递归返回阶段）
        if (!context.shouldTerminate()) {
            AudioData postprocessed = postProcess(result, context);
            context.log("后处理完成: " + postprocessed);
            System.out.println("← 返回 " + getProcessorName() + " | " + postprocessed);
            return postprocessed;
        }

        System.out.println("← 返回 " + getProcessorName() + " [已中断] | " + result);
        return result;
    }

    protected AudioData preProcess(AudioData input, ProcessingContext context) {
        // 默认不进行预处理
        return input;
    }

    protected AudioData postProcess(AudioData input, ProcessingContext context) {
        // 默认不进行后处理
        return input;
    }

    public abstract String getProcessorName();
}
