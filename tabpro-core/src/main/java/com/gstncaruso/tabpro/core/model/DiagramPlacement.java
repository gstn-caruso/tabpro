package com.gstncaruso.tabpro.core.model;

/** Donde se dibujan los diagramas de acordes de una pista. */
public enum DiagramPlacement {
    ABOVE_THE_STAFF("Sobre el pentagrama"),
    UNDER_THE_TITLE("Debajo del titulo"),
    BOTH("En los dos lados"),
    HIDDEN("Ocultos");

    private final String label;

    DiagramPlacement(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean showsOnTheScore() {
        return this == ABOVE_THE_STAFF || this == BOTH;
    }

    public boolean showsUnderTheTitle() {
        return this == UNDER_THE_TITLE || this == BOTH;
    }
}
