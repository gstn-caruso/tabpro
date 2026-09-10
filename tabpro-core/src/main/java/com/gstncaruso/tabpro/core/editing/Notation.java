package com.gstncaruso.tabpro.core.editing;

public enum Notation {
    TABLATURE,
    STANDARD;

    public Notation other() {
        return this == TABLATURE ? STANDARD : TABLATURE;
    }
}
