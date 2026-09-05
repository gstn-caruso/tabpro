package com.gstncaruso.tabpro.core.model.bars;

/** Los destinos que se marcan en un compas para que un salto pueda apuntarles. */
public enum DirectionSymbol {
    CODA("Coda"),
    DOUBLE_CODA("Doble coda"),
    SEGNO("Segno"),
    SEGNO_SEGNO("Segno segno"),
    FINE("Fine");

    private final String label;

    DirectionSymbol(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
