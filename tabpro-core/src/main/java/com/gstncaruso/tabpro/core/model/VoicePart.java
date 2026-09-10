package com.gstncaruso.tabpro.core.model;

public enum VoicePart {
    LEAD("Voz 1"),
    BASS("Voz 2");

    private final String label;

    VoicePart(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public VoicePart other() {
        return this == LEAD ? BASS : LEAD;
    }
}
