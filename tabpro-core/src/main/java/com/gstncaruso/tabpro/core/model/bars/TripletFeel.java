package com.gstncaruso.tabpro.core.model.bars;

public enum TripletFeel {
    NONE("Ninguno"),
    EIGHTH("Corcheas con swing"),
    SIXTEENTH("Semicorcheas con swing");

    private final String label;

    TripletFeel(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean swings() {
        return this != NONE;
    }
}
