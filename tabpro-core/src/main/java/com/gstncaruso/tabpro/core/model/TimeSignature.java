package com.gstncaruso.tabpro.core.model;

public record TimeSignature(int beats, int beatUnit) {

    public TimeSignature {
        if (beats < 1) {
            throw new IllegalArgumentException("beats debe ser >= 1: " + beats);
        }
        if (!isPowerOfTwoUpTo64(beatUnit)) {
            throw new IllegalArgumentException("beatUnit debe ser una potencia de 2 entre 1 y 64: " + beatUnit);
        }
    }

    private static boolean isPowerOfTwoUpTo64(int value) {
        return value >= 1 && value <= 64 && (value & (value - 1)) == 0;
    }

    public static TimeSignature fourFour() {
        return new TimeSignature(4, 4);
    }

    public long ticksPerMeasure() {
        return beats * (Duration.TICKS_PER_QUARTER * 4L / beatUnit);
    }
}
