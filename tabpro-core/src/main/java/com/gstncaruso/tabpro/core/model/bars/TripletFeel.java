package com.gstncaruso.tabpro.core.model.bars;

/** La sincopa que se toca aunque no se escriba: el swing de corcheas o de semicorcheas. */
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
