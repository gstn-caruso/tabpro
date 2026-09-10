package com.gstncaruso.tabpro.core.model.effects;

public enum BeamBreak {
    AUTOMATIC("Automático"),
    FORCED("Forzar corte"),
    PREVENTED("Impedir corte");

    private final String label;

    BeamBreak(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
