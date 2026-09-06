package com.gstncaruso.tabpro.core.tuning;

/**
 * Escucha una muestra de audio y dice qué nota suena. Usa la diferencia
 * cuadrática promedio normalizada: se compara la onda consigo misma corrida en
 * el tiempo, y el corrimiento donde mejor se parece es el período.
 */
public final class PitchDetector {

    /** El rango donde puede caer una cuerda de guitarra o de bajo, con aire de sobra. */
    public static final double LOWEST_HZ = 30;
    public static final double HIGHEST_HZ = 1400;

    /** Por debajo de esto lo que llega es silencio, no una cuerda. */
    private static final double SILENCE = 0.005;

    private final int sampleRate;

    public PitchDetector(int sampleRate) {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("sampleRate debe ser > 0: " + sampleRate);
        }
        this.sampleRate = sampleRate;
    }

    public DetectedPitch detect(double[] samples) {
        if (samples.length < 2 || isSilent(samples)) {
            return new DetectedPitch(0, 0);
        }
        int shortestPeriod = (int) Math.floor(sampleRate / HIGHEST_HZ);
        int longestPeriod = (int) Math.ceil(sampleRate / LOWEST_HZ);
        int lastPeriod = Math.min(longestPeriod, samples.length / 2);
        if (shortestPeriod >= lastPeriod) {
            return new DetectedPitch(0, 0);
        }

        double[] difference = normalisedDifference(samples, lastPeriod);
        int period = firstValley(difference, shortestPeriod, lastPeriod);
        if (period < 0) {
            return new DetectedPitch(0, 0);
        }
        double refined = refined(difference, period);
        return new DetectedPitch(sampleRate / refined, 1 - difference[period]);
    }

    private static boolean isSilent(double[] samples) {
        double energy = 0;
        for (double sample : samples) {
            energy += sample * sample;
        }
        return Math.sqrt(energy / samples.length) < SILENCE;
    }

    /** Para cada corrimiento, cuánto se diferencia la onda de sí misma, de 0 a 1. */
    private static double[] normalisedDifference(double[] samples, int lastPeriod) {
        double[] difference = new double[lastPeriod + 1];
        double runningSum = 0;
        for (int period = 1; period <= lastPeriod; period++) {
            double squared = 0;
            for (int index = 0; index + period < samples.length; index++) {
                double gap = samples[index] - samples[index + period];
                squared += gap * gap;
            }
            runningSum += squared;
            difference[period] = runningSum == 0 ? 1 : squared * period / runningSum;
        }
        difference[0] = 1;
        return difference;
    }

    /** El primer corrimiento donde la onda se parece bastante a sí misma. */
    private static int firstValley(double[] difference, int from, int to) {
        double threshold = 1 - DetectedPitch.MIN_CLARITY;
        for (int period = from; period <= to; period++) {
            if (difference[period] < threshold) {
                int valley = period;
                while (valley + 1 <= to && difference[valley + 1] < difference[valley]) {
                    valley++;
                }
                return valley;
            }
        }
        return -1;
    }

    /** Afina el período con la parábola que pasa por el valle y sus vecinos. */
    private static double refined(double[] difference, int period) {
        if (period <= 0 || period + 1 >= difference.length) {
            return period;
        }
        double before = difference[period - 1];
        double at = difference[period];
        double after = difference[period + 1];
        double curvature = before + after - 2 * at;
        if (curvature == 0) {
            return period;
        }
        return period + (before - after) / (2 * curvature);
    }
}
