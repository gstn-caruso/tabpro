package com.gstncaruso.tabpro.ui.dialogs.metronome;

public record MetronomeSettings(boolean active, int volume) {

    public static final int MIN_VOLUME = 0;
    public static final int MAX_VOLUME = 127;

    public MetronomeSettings {
        if (volume < MIN_VOLUME || volume > MAX_VOLUME) {
            throw new IllegalArgumentException("volume debe estar entre " + MIN_VOLUME + " y " + MAX_VOLUME + ": " + volume);
        }
    }

    public static MetronomeSettings off() {
        return new MetronomeSettings(false, 100);
    }
}
