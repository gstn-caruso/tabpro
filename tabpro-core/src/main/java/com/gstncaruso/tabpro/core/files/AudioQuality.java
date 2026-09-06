package com.gstncaruso.tabpro.core.files;

/**
 * La calidad del archivo que pide "File > Export > Wave" del manual: cuantas muestras
 * por segundo, cuantos bits por muestra y si el archivo sale en mono o en estereo.
 */
public record AudioQuality(int sampleRateHz, int bitDepth, int channels) {

    /** 44.1 kHz, 16 bits, estereo: la calidad de un CD de audio. */
    public static AudioQuality standard() {
        return new AudioQuality(44_100, 16, 2);
    }
}
