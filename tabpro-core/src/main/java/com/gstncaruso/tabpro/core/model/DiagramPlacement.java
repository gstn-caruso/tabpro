package com.gstncaruso.tabpro.core.model;

public enum DiagramPlacement {
    ABOVE_THE_STAFF("Sobre el pentagrama"),
    UNDER_THE_TITLE("Debajo del título"),
    BOTH("En los dos lados"),
    HIDDEN("Ocultos");

    private final String label;

    DiagramPlacement(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static DiagramPlacement of(boolean onTheScore, boolean underTheTitle) {
        if (onTheScore && underTheTitle) {
            return BOTH;
        }
        if (onTheScore) {
            return ABOVE_THE_STAFF;
        }
        if (underTheTitle) {
            return UNDER_THE_TITLE;
        }
        return HIDDEN;
    }

    public boolean showsOnTheScore() {
        return this == ABOVE_THE_STAFF || this == BOTH;
    }

    public boolean showsUnderTheTitle() {
        return this == UNDER_THE_TITLE || this == BOTH;
    }
}
