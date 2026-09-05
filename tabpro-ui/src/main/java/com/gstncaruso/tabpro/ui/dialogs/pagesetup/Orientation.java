package com.gstncaruso.tabpro.ui.dialogs.pagesetup;

public enum Orientation {
    PORTRAIT("Vertical"),
    LANDSCAPE("Horizontal");

    private final String label;

    Orientation(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
