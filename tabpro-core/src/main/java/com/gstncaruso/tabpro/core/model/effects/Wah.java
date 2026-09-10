package com.gstncaruso.tabpro.core.model.effects;

public enum Wah {
    OPEN("Abierto"),
    CLOSED("Cerrado"),
    OFF("Apagado");

    private final String label;

    Wah(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
