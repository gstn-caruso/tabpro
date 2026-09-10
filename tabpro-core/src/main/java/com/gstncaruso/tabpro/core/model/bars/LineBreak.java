package com.gstncaruso.tabpro.core.model.bars;

public enum LineBreak {
    AUTOMATIC("Automático"),
    FORCED("Forzar salto"),
    PREVENTED("Impedir salto");

    private final String label;

    LineBreak(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
