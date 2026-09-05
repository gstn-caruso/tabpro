package com.gstncaruso.tabpro.core.model.effects;

/** El pedal de wah-wah, que se prende, se cierra, se abre o se apaga. */
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
