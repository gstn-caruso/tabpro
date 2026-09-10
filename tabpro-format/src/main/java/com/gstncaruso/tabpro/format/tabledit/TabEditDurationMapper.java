package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;

/**
 * Translates TablEdit's note-value code (0 to 31) into a {@link Duration}. TablEdit
 * numbers its note values as a single scale: every three codes drop one level (whole,
 * half, quarter, ...), and within each level the remainder of dividing by three says
 * whether it is the simple note value, the dotted one, or the triplet. A handful of
 * codes are pure filler, and four others ask for a double dot the tabpro model cannot represent.
 */
final class TabEditDurationMapper {

    private static final NoteValue[] VALUES_FROM_LONGEST = {
            NoteValue.WHOLE, NoteValue.HALF, NoteValue.QUARTER, NoteValue.EIGHTH,
            NoteValue.SIXTEENTH, NoteValue.THIRTY_SECOND, NoteValue.SIXTY_FOURTH,
    };

    private static final int DOTTED_WHOLE = 31;

    private TabEditDurationMapper() {
    }

    static Duration toDuration(int code) {
        if (isFillerFor(code, NoteValue.SIXTEENTH)) {
            return new Duration(NoteValue.SIXTEENTH, false);
        }
        if (isFillerFor(code, NoteValue.SIXTY_FOURTH)) {
            return new Duration(NoteValue.SIXTY_FOURTH, false);
        }
        if (code == DOTTED_WHOLE) {
            return new Duration(NoteValue.WHOLE, true);
        }
        if (isDoubleDotted(code)) {
            throw new ScoreFileException(
                    "esta partitura usa una figura con doble puntillo (codigo " + code
                            + " de TablEdit), que tabpro no puede representar todavia.");
        }
        if (code < 0 || code > 18) {
            throw new ScoreFileException("codigo de figura de TablEdit fuera de rango: " + code);
        }

        int level = code / 3;
        int remainder = code % 3;
        boolean dotted = remainder == 1;
        int valueIndex = dotted ? level + 1 : level;
        NoteValue value = VALUES_FROM_LONGEST[valueIndex];
        Tuplet tuplet = remainder == 2 ? Tuplet.of(3) : Tuplet.none();

        return new Duration(value, dotted, tuplet);
    }

    /** Codes 20/23/26/29 repeat the sixteenth note; 21/24/27/30 repeat the sixty-fourth. */
    private static boolean isFillerFor(int code, NoteValue value) {
        int base = value == NoteValue.SIXTEENTH ? 20 : 21;
        return code == base || code == base + 3 || code == base + 6 || code == base + 9;
    }

    private static boolean isDoubleDotted(int code) {
        return code == 19 || code == 22 || code == 25 || code == 28;
    }
}
