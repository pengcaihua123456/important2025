package com.example.design.chain.use;

// ====================== 核心数据结构 ======================
public class AudioData {
    private String rawData;
    private double snr; // 信噪比
    private double[] frequencySpectrum;
    private boolean processed = false;
    private String processingHistory = "";

    public AudioData(String rawData) {
        this.rawData = rawData;
        this.snr = 10.0; // 初始信噪比
    }

    public void applyFilter(String filterName, double snrImprovement) {
        this.snr += snrImprovement;
        this.processed = true;
        processingHistory += " → " + filterName + " (+" + snrImprovement + " dB)";
    }

    public AudioData applyFilter(String filterName) {
        // 模拟处理效果
        System.out.println("    ├─ 应用滤波器: " + filterName);

        // 根据滤波器类型更新状态
        switch (filterName) {
            case "环境噪声检测":
                this.snr += 5.0;
                break;
            case "回声消除":
                this.snr += 8.0;
                break;
            case "频谱降噪":
                this.snr += 12.0;
                break;
            case "语音增强":
                this.snr += 7.0;
                break;
            case "动态范围压缩":
                this.snr += 3.0;
                break;
            case "风噪抑制":
                this.snr += 6.0;
                break;
        }

        this.processed = true;
        return this;
    }

    public double getSNR() {
        return snr;
    }

    public String getStatus() {
        return processed ? "[已处理] SNR: " + String.format("%.1f", snr) + " dB" : "[原始数据]";
    }

    @Override
    public String toString() {
        return "音频数据 " + getStatus();
    }
}

