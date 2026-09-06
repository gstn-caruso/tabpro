package com.gstncaruso.tabpro.core.model.effects;

/** Las seis maneras de deslizar el dedo que distingue la tablatura. */
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

    /** Si el destino del slide es la nota siguiente, y no un traste indefinido. */
    public boolean towardsTheNextNote() {
        return towardsTheNextNote;
    }

    /** Si la nota de destino se ataca de nuevo, en lugar de sonar por el deslizamiento. */
    public boolean picksTheDestination() {
        return this == SHIFT;
    }
}
