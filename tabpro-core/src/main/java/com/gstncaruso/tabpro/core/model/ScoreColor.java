package com.gstncaruso.tabpro.core.model;

/** Un color de la partitura, sin depender de la interfaz que lo dibuje. */
public record ScoreColor(int red, int green, int blue) {

    public ScoreColor {
        requireByte(red, "red");
        requireByte(green, "green");
        requireByte(blue, "blue");
    }

    public static ScoreColor rgb(int packed) {
        return new ScoreColor((packed >> 16) & 0xFF, (packed >> 8) & 0xFF, packed & 0xFF);
    }

    public int packed() {
        return (red << 16) | (green << 8) | blue;
    }

    private static void requireByte(int value, String name) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(name + " debe estar entre 0 y 255: " + value);
        }
    }
}
