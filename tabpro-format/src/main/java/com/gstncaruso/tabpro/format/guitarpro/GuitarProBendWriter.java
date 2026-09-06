package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;

/**
 * Escribe la curva de un bend o de una palanca: el espejo de {@link GuitarProBendReader}.
 * Todos los tipos de bend que reconoce tabpro tienen un codigo propio en el formato: no
 * se pierde nada al exportar.
 */
final class GuitarProBendWriter {

    private static final int UNITS_PER_QUARTER_TONE = 50;

    void write(GuitarProByteWriter writer, Bend bend) {
        writer.writeSignedByte(codeOf(bend.type()));
        writer.writeInt(bend.peakQuarterTones() * UNITS_PER_QUARTER_TONE); // profundidad general: solo informativa.
        writer.writeInt(bend.points().size());
        for (BendPoint point : bend.points()) {
            writer.writeInt(point.position());
            writer.writeInt(point.quarterTones() * UNITS_PER_QUARTER_TONE);
            writer.writeUnsignedByte(point.vibrato());
        }
    }

    private static int codeOf(BendType type) {
        return switch (type) {
            case BEND -> 1;
            case BEND_RELEASE -> 2;
            case BEND_RELEASE_BEND -> 3;
            case PREBEND -> 4;
            case PREBEND_RELEASE -> 5;
        };
    }
}
