package com.gstncaruso.tabpro.core.model.effects;

public enum StemOverride {
    AUTOMATIC("Automático"),
    UP("Arriba"),
    DOWN("Abajo");

    private final String label;

    StemOverride(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean pointsUp(boolean automaticPointsUp) {
        return switch (this) {
            case UP -> true;
            case DOWN -> false;
            case AUTOMATIC -> automaticPointsUp;
        };
    }
}
