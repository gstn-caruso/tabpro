package com.gstncaruso.tabpro.midi;

import java.util.Optional;

public record SoundFontStatus(Kind kind, Optional<String> fileName) {

    public enum Kind {
        NONE, CHOSEN, DISABLED, PLAYING, FAILED
    }

    public static SoundFontStatus none() {
        return new SoundFontStatus(Kind.NONE, Optional.empty());
    }

    public static SoundFontStatus chosen(String fileName) {
        return new SoundFontStatus(Kind.CHOSEN, Optional.of(fileName));
    }

    public static SoundFontStatus disabled(String fileName) {
        return new SoundFontStatus(Kind.DISABLED, Optional.of(fileName));
    }

    public static SoundFontStatus playing(String fileName) {
        return new SoundFontStatus(Kind.PLAYING, Optional.of(fileName));
    }

    public static SoundFontStatus failed(String fileName) {
        return new SoundFontStatus(Kind.FAILED, Optional.of(fileName));
    }
}
