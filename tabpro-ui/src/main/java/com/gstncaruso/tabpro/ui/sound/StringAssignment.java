package com.gstncaruso.tabpro.ui.sound;

public enum StringAssignment {
    FIRST_CHANNEL_IS_THE_HIGHEST_STRING("El primer canal es la cuerda más aguda"),
    FIRST_CHANNEL_IS_THE_LOWEST_STRING("El primer canal es la cuerda más grave"),
    NO_CHANNEL_DETECTION("Sin detección de canal");

    private final String label;

    StringAssignment(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public java.util.OptionalInt stringFor(int channel, int stringCount) {
        return switch (this) {
            case FIRST_CHANNEL_IS_THE_HIGHEST_STRING -> withinReach(channel + 1, stringCount);
            case FIRST_CHANNEL_IS_THE_LOWEST_STRING -> withinReach(stringCount - channel, stringCount);
            case NO_CHANNEL_DETECTION -> java.util.OptionalInt.empty();
        };
    }

    private static java.util.OptionalInt withinReach(int string, int stringCount) {
        return string >= 1 && string <= stringCount
                ? java.util.OptionalInt.of(string)
                : java.util.OptionalInt.empty();
    }
}
