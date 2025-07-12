package com.example.design.chain.use;

// ====================== 高级处理链构建器 ======================
class AudioProcessingChainBuilder {
    private AudioProcessor first;
    private AudioProcessor current; // 是最后一个

    public AudioProcessingChainBuilder addProcessor(AudioProcessor processor) {
        if (first == null) {
            first = processor;
            current = processor;
        } else {
            current.setNext(processor);
            current = processor;
        }
        return this;
    }

    public AudioProcessor build() { // 拿到第一个
        return first;
    }
}
