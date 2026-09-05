package com.gstncaruso.tabpro.core.model.bars;

/** Como se decide si este compas empieza un renglon nuevo. */
public enum LineBreak {
    AUTOMATIC("Automatico"),
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
