package com.gstncaruso.tabpro.format.exchange.musicxml;

import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.NoteValue;

/** The note-value name MusicXML uses in {@code <type>}, which is not the same text as the enum. */
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
            default -> throw ScoreFileException.unsupportedContent(
                    ScoreFeature.EXTREME_NOTE_VALUES, "unsupported MusicXML note type: " + type);
        };
    }
}
