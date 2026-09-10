package com.gstncaruso.tabpro.core.model.effects;

public enum StrokeDirection {
    DOWN,
    UP;

    public boolean startsAtTheLowestString() {
        return this == DOWN;
    }
}
