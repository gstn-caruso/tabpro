package com.gstncaruso.tabpro.core.model.chords;

public enum ChordComplexity {
    SIMPLE,
    MEDIUM,
    COMPLEX;

    public boolean accepts(ChordComplexity actual) {
        return actual.ordinal() <= this.ordinal();
    }
}
