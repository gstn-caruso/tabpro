package com.gstncaruso.tabpro.core.model;

public enum NoteValue {
    WHOLE(1),
    HALF(2),
    QUARTER(4),
    EIGHTH(8),
    SIXTEENTH(16),
    THIRTY_SECOND(32),
    SIXTY_FOURTH(64);

    private final int denominator;

    NoteValue(int denominator) {
        this.denominator = denominator;
    }

    public int denominator() {
        return denominator;
    }

    public NoteValue longer() {
        int index = ordinal();
        return index == 0 ? this : values()[index - 1];
    }

    public NoteValue shorter() {
        int index = ordinal();
        NoteValue[] values = values();
        return index == values.length - 1 ? this : values[index + 1];
    }
}
