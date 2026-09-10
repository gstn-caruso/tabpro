package com.gstncaruso.tabpro.core.model.effects;

public enum SlideType {
    LEGATO(true),
    SHIFT(true),
    IN_FROM_BELOW(false),
    IN_FROM_ABOVE(false),
    OUT_DOWNWARDS(false),
    OUT_UPWARDS(false);

    private final boolean towardsTheNextNote;

    SlideType(boolean towardsTheNextNote) {
        this.towardsTheNextNote = towardsTheNextNote;
    }

    public boolean towardsTheNextNote() {
        return towardsTheNextNote;
    }

    public boolean picksTheDestination() {
        return this == SHIFT;
    }
}
