package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;

/**
 * Traduce el codigo de figura de TablEdit (0 a 31) a una {@link Duration}.
 * TablEdit numera sus figuras como una sola escala: cada tres codigos bajan un
 * nivel (redonda, blanca, negra, ...), y dentro de cada nivel el resto de
 * dividir por tres dice si es la figura simple, la punteada o el tresillo.
 * Un puñado de codigos son puro relleno, y otros cuatro piden un doble
 * puntillo que el modelo de tabpro no puede representar.
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

    /** Los codigos 20/23/26/29 repiten la semicorchea; 21/24/27/30 repiten la semifusa. */
    private static boolean isFillerFor(int code, NoteValue value) {
        int base = value == NoteValue.SIXTEENTH ? 20 : 21;
        return code == base || code == base + 3 || code == base + 6 || code == base + 9;
    }

    private static boolean isDoubleDotted(int code) {
        return code == 19 || code == 22 || code == 25 || code == 28;
    }
}
