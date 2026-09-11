package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.ui.i18n.Texts;

public enum StringAssignment {
    FIRST_CHANNEL_IS_THE_HIGHEST_STRING,
    FIRST_CHANNEL_IS_THE_LOWEST_STRING,
    NO_CHANNEL_DETECTION;

    public String label() {
        return Texts.get("views.StringAssignment." + name());
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
