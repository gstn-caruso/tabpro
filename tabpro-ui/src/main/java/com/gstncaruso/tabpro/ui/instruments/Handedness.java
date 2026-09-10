package com.gstncaruso.tabpro.ui.instruments;

public enum Handedness {
    RIGHT_HANDED,
    LEFT_HANDED;

    public int mirror(int x, int totalWidth) {
        return this == LEFT_HANDED ? totalWidth - x : x;
    }
}
