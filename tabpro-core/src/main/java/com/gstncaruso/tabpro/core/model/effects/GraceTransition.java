package com.gstncaruso.tabpro.core.model.effects;

public enum GraceTransition {
    NONE("Ninguna"),
    SLIDE("Slide"),
    BEND("Bend"),
    HAMMER("Ligado");

    private final String label;

    GraceTransition(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
