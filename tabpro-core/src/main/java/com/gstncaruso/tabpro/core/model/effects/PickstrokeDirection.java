package com.gstncaruso.tabpro.core.model.effects;

public enum PickstrokeDirection {
    DOWN("Hacia abajo"),
    UP("Hacia arriba");

    private final String label;

    PickstrokeDirection(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
