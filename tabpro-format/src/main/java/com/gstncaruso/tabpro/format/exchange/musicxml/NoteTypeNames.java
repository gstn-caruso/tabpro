package com.gstncaruso.tabpro.format.exchange.musicxml;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.NoteValue;

/** El nombre de figura que usa MusicXML en {@code <type>}, que no es el mismo texto que el enum. */
final class NoteTypeNames {

    private NoteTypeNames() {
    }

    static String toXml(NoteValue value) {
        return switch (value) {
            case WHOLE -> "whole";
            case HALF -> "half";
            case QUARTER -> "quarter";
            case EIGHTH -> "eighth";
            case SIXTEENTH -> "16th";
            case THIRTY_SECOND -> "32nd";
            case SIXTY_FOURTH -> "64th";
        };
    }

    static NoteValue fromXml(String type) {
        return switch (type) {
            case "whole" -> NoteValue.WHOLE;
            case "half" -> NoteValue.HALF;
            case "quarter" -> NoteValue.QUARTER;
            case "eighth" -> NoteValue.EIGHTH;
            case "16th" -> NoteValue.SIXTEENTH;
            case "32nd" -> NoteValue.THIRTY_SECOND;
            case "64th" -> NoteValue.SIXTY_FOURTH;
            default -> throw new ScoreFileException("figura de MusicXML no soportada: " + type);
        };
    }
}
