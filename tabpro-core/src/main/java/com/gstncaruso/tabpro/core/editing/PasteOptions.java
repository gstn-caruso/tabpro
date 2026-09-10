package com.gstncaruso.tabpro.core.editing;

public record PasteOptions(boolean inserting, int repetitions) {

    public PasteOptions {
        if (repetitions < 1) {
            throw new IllegalArgumentException("must paste at least once: " + repetitions);
        }
    }

    public static PasteOptions replacingOnce() {
        return new PasteOptions(false, 1);
    }

    public static PasteOptions insertingOnce() {
        return new PasteOptions(true, 1);
    }
}
