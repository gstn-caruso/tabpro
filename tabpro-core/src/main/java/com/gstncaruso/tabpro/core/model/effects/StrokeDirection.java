package com.gstncaruso.tabpro.core.model.effects;

/** Hacia donde barre la mano derecha. */
public enum StrokeDirection {
    DOWN("Hacia abajo"),
    UP("Hacia arriba");

    private final String label;

    StrokeDirection(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** El rasgueo hacia abajo arranca por la cuerda mas grave. */
    public boolean startsAtTheLowestString() {
        return this == DOWN;
    }
}
