package com.gstncaruso.tabpro.core.model;

public enum VoicePart {
    LEAD,
    BASS;

    public VoicePart other() {
        return this == LEAD ? BASS : LEAD;
    }
}
