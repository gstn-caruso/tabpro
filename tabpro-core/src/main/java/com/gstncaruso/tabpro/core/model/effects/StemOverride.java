package com.gstncaruso.tabpro.core.model.effects;

public enum StemOverride {
    AUTOMATIC,
    UP,
    DOWN;

    public boolean pointsUp(boolean automaticPointsUp) {
        return switch (this) {
            case UP -> true;
            case DOWN -> false;
            case AUTOMATIC -> automaticPointsUp;
        };
    }
}
