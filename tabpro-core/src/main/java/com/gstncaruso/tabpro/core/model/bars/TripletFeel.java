package com.gstncaruso.tabpro.core.model.bars;

public enum TripletFeel {
    NONE,
    EIGHTH,
    SIXTEENTH;

    public boolean swings() {
        return this != NONE;
    }
}
