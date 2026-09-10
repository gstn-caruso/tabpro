package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.core.model.NoteValue;

/**
 * El unico punto que traduce un tipo del dominio a su texto en castellano: ningun combo
 * ni lista de tabpro debe apoyarse en el toString() de un enum o de un record.
 */
public final class Labels {

    private Labels() {
    }

    public static String of(Object value) {
        return switch (value) {
            case NoteValue noteValue -> noteValueLabel(noteValue);
            default -> throw new IllegalArgumentException("Sin etiqueta para " + value);
        };
    }

    private static String noteValueLabel(NoteValue value) {
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
