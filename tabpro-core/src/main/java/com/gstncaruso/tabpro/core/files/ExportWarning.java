package com.gstncaruso.tabpro.core.files;

import java.util.List;

public record ExportWarning(ExportWarning.Loss loss, List<Object> arguments) {

    public enum Loss {
        MUSIC_AUTHOR,
        SECOND_VOICE,
        TRIPLET_FEEL_CHANGES,
        TRACK_DISPLAY,
        MIDI_PORT,
        WIDE_VIBRATO,
        CHORD_NAME_ONLY,
        GRACE_NOTE_ON_BEAT_OR_DEAD
    }

    public static ExportWarning of(Loss loss, Object... arguments) {
        return new ExportWarning(loss, List.of(arguments));
    }
}
