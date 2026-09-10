package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.ui.harmony.BarrePreference;

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
            case ChordType chordType -> chordTypeLabel(chordType);
            case ChordComplexity chordComplexity -> chordComplexityLabel(chordComplexity);
            case BarrePreference barrePreference -> barrePreferenceLabel(barrePreference);
            case PitchClass pitchClass -> pitchClass.name() + " (" + pitchClass.solfegeName() + ")";
            case Scale scale -> scale.name();
            default -> throw new IllegalArgumentException("Sin etiqueta para " + value);
        };
    }

    private static String barrePreferenceLabel(BarrePreference value) {
        return switch (value) {
            case ANY -> "Cualquiera";
            case FORCE -> "Forzar cejilla";
            case FORBID -> "Prohibir cejilla";
        };
    }

    /** El sufijo con que el manual nombra el tipo de acorde: "M" para el mayor, "m7", "sus4"... */
    private static String chordTypeLabel(ChordType value) {
        return value == ChordType.MAJOR ? "M" : value.suffix();
    }

    private static String chordComplexityLabel(ChordComplexity value) {
        return switch (value) {
            case SIMPLE -> "Simple";
            case MEDIUM -> "Media";
            case COMPLEX -> "Todas";
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
