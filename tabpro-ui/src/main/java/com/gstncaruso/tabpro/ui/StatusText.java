package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;

public final class StatusText {

    private StatusText() {
    }

    public static String describe(Cursor cursor, Beat beat) {
        return "Compás " + (cursor.measure() + 1)
                + " · Beat " + (cursor.beat() + 1)
                + " · Cuerda " + cursor.string()
                + " · " + describe(beat);
    }

    private static String describe(Beat beat) {
        String name = nameOf(beat.duration());
        return beat.isRest() ? "Silencio de " + name.toLowerCase() : name;
    }

    private static String nameOf(Duration duration) {
        String name = figureName(duration.value());
        return duration.dotted() ? name + " con puntillo" : name;
    }

    private static String figureName(NoteValue value) {
        return switch (value) {
            case WHOLE -> "Redonda";
            case HALF -> "Blanca";
            case QUARTER -> "Negra";
            case EIGHTH -> "Corchea";
            case SIXTEENTH -> "Semicorchea";
            case THIRTY_SECOND -> "Fusa";
            case SIXTY_FOURTH -> "Semifusa";
        };
    }
}
