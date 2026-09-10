package com.gstncaruso.tabpro.core.model.effects;

public enum Ornament {
    GHOST("Nota fantasma"),
    DEAD("Nota muerta"),
    ACCENTED("Acentuada"),
    HEAVY_ACCENTED("Muy acentuada"),
    STACCATO("Staccato"),
    PALM_MUTE("Palm mute"),
    LET_RING("Let ring"),
    VIBRATO("Vibrato"),
    HAMMER_ON_PULL_OFF("Ligado");

    private final String label;

    Ornament(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
