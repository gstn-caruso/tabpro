package com.gstncaruso.tabpro.core.model.bars;

public enum Mode {
    MAJOR("Mayor"),
    MINOR("Menor");

    private final String label;

    Mode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
