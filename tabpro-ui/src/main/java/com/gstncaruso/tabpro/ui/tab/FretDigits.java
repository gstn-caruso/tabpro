package com.gstncaruso.tabpro.ui.tab;

import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.function.IntPredicate;
import java.util.function.LongSupplier;

public final class FretDigits {

    public static final long DEFAULT_WINDOW_MILLIS = 700;

    private final LongSupplier clockMillis;
    private final long windowMillis;
    private Integer pendingFret;
    private long pendingAtMillis;

    public FretDigits(LongSupplier clockMillis) {
        this(clockMillis, DEFAULT_WINDOW_MILLIS);
    }

    public FretDigits(LongSupplier clockMillis, long windowMillis) {
        this.clockMillis = clockMillis;
        this.windowMillis = windowMillis;
    }

    /** El traste de una pista de guitarra: dos digitos combinan si el resultado no pasa el ultimo traste. */
    public int fretFor(char digit) {
        return fretFor(digit, combined -> combined <= Tuning.MAX_FRET);
    }

    /** El numero de una pista cualquiera: dos digitos combinan si el resultado le sirve a esa pista. */
    public int fretFor(char digit, IntPredicate combinable) {
        int typed = digit - '0';
        int combined = pendingFret == null ? -1 : pendingFret * 10 + typed;
        if (pendingFret != null && withinWindow() && combinable.test(combined)) {
            reset();
            return combined;
        }
        pendingFret = typed;
        pendingAtMillis = clockMillis.getAsLong();
        return typed;
    }

    public void reset() {
        pendingFret = null;
    }

    private boolean withinWindow() {
        return clockMillis.getAsLong() - pendingAtMillis < windowMillis;
    }
}
