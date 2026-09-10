package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;

/**
 * The mirror of {@link GuitarProBendReader}. Every bend type tabpro recognizes has its
 * own code in the format: nothing is lost on export.
 */
final class GuitarProBendWriter {

    private static final int UNITS_PER_QUARTER_TONE = 25;

    void write(GuitarProByteWriter writer, Bend bend) {
        writer.writeSignedByte(codeOf(bend.type()));
        writer.writeInt(bend.peakQuarterTones() * UNITS_PER_QUARTER_TONE); // overall depth: informational only.
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
            case DIP -> 6;
            case DIVE -> 7;
            case RELEASE_UP -> 8;
            case INVERTED_DIP -> 9;
            case RETURN -> 10;
            case RELEASE_DOWN -> 11;
        };
    }
}
