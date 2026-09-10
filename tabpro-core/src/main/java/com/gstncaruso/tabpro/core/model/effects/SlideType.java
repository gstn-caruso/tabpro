package com.gstncaruso.tabpro.core.model.effects;

public enum SlideType {
    LEGATO("Slide legato", true),
    SHIFT("Slide con ataque", true),
    IN_FROM_BELOW("Entrando desde abajo", false),
    IN_FROM_ABOVE("Entrando desde arriba", false),
    OUT_DOWNWARDS("Saliendo hacia abajo", false),
    OUT_UPWARDS("Saliendo hacia arriba", false);

    private final String label;
    private final boolean towardsTheNextNote;

    SlideType(String label, boolean towardsTheNextNote) {
        this.label = label;
        this.towardsTheNextNote = towardsTheNextNote;
    }

    public String label() {
        return label;
    }

    public boolean towardsTheNextNote() {
        return towardsTheNextNote;
    }

    public boolean picksTheDestination() {
        return this == SHIFT;
    }
}
