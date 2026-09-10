package com.gstncaruso.tabpro.core.model;

import java.util.List;

public record Tuplet(int enters, int inTheTimeOf) {

    private static final Tuplet NONE = new Tuplet(1, 1);

    public static final List<Integer> AVAILABLE = List.of(1, 3, 5, 6, 7, 9, 10, 11, 12, 13);

    public Tuplet {
        if (enters < 1 || inTheTimeOf < 1) {
            throw new IllegalArgumentException("an irregular group needs positive amounts");
        }
    }

    public static Tuplet none() {
        return NONE;
    }

    public static Tuplet of(int enters) {
        if (!AVAILABLE.contains(enters)) {
            throw new IllegalArgumentException("irregular group not supported: " + enters);
        }
        return enters == 1 ? NONE : new Tuplet(enters, Integer.highestOneBit(enters - 1));
    }

    public boolean isPlain() {
        return equals(NONE);
    }

    public long apply(long ticks) {
        return ticks * inTheTimeOf / enters;
    }
}
