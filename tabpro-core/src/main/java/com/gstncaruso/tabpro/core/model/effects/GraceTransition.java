package com.gstncaruso.tabpro.core.model.effects;

/** Como se llega desde la nota de adorno hasta la nota principal. */
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
