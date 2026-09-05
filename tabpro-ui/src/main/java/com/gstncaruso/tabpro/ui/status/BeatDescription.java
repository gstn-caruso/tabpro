package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;

/**
 * Describe en una linea donde esta parado el cursor: compas, beat, cuerda y la figura que suena.
 *
 * <p>Es la misma logica de {@link com.gstncaruso.tabpro.ui.StatusText}, portada a este paquete
 * para que la barra de estado nueva la use sin depender de la clase vieja.
 */
public final class BeatDescription {

    private BeatDescription() {
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
