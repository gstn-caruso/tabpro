package com.gstncaruso.tabpro.core.files;

public record AudioQuality(int sampleRateHz, int bitDepth, int channels) {

    public static AudioQuality standard() {
        return new AudioQuality(44_100, 16, 2);
    }
}
