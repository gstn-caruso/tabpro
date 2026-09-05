package com.gstncaruso.tabpro.core.editing;

/** Lo que pregunta la ventana de pegar: si inserta o reemplaza, y cuantas veces. */
public record PasteOptions(boolean inserting, int repetitions) {

    public PasteOptions {
        if (repetitions < 1) {
            throw new IllegalArgumentException("hay que pegar al menos una vez: " + repetitions);
        }
    }

    public static PasteOptions replacingOnce() {
        return new PasteOptions(false, 1);
    }

    public static PasteOptions insertingOnce() {
        return new PasteOptions(true, 1);
    }
}
