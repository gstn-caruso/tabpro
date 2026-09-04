package com.gstncaruso.tabpro.core.model;

public record Note(int string, int fret) {

    public static final int MAX_FRET = 24;

    public Note {
        if (string < 1) {
            throw new IllegalArgumentException("string debe ser >= 1: " + string);
        }
        if (fret < 0 || fret > MAX_FRET) {
            throw new IllegalArgumentException("fret debe estar entre 0 y " + MAX_FRET + ": " + fret);
        }
    }
}
