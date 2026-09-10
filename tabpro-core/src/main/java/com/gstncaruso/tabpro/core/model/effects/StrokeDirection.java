package com.gstncaruso.tabpro.core.model.effects;

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

    public boolean startsAtTheLowestString() {
        return this == DOWN;
    }
}
