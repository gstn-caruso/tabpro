package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import java.util.ArrayList;
import java.util.List;

/**
 * Guitar Pro stores the bend or tremolo bar curve position from 0 to 60 (same as
 * {@link BendPoint#LAST_POSITION}) and the depth in units of which 25 make a quarter
 * tone: a whole tone, the full bend, is written as 100. Tabpro stores it in quarter tones.
 */
final class GuitarProBendReader {

    private static final int UNITS_PER_QUARTER_TONE = 25;

    Bend read(GuitarProByteReader reader) {
        int rawType = reader.readSignedByte();
        reader.readInt(); // overall curve depth: derivable from the points.
        int pointCount = Math.max(2, reader.readInt());
        List<BendPoint> points = new ArrayList<>(pointCount);
        for (int i = 0; i < pointCount; i++) {
            points.add(readPoint(reader));
        }
        while (points.size() < 2) {
            points.add(BendPoint.at(BendPoint.LAST_POSITION, 0));
        }
        return new Bend(bendTypeOf(rawType), points);
    }

    /**
     * The GP3 tremolo bar has no curve: it is a single integer with how far the string
     * dips. It is given the shape Guitar Pro draws it with -- it dips to the middle of
     * the note and returns to pitch.
     */
    Bend readOldTremoloBar(GuitarProByteReader reader) {
        int depth = quarterTonesOf(-reader.readInt());
        return new Bend(BendType.BEND_RELEASE, List.of(
                BendPoint.at(0, 0),
                BendPoint.at(BendPoint.LAST_POSITION / 2, depth),
                BendPoint.at(BendPoint.LAST_POSITION, 0)));
    }

    private BendPoint readPoint(GuitarProByteReader reader) {
        int position = Math.clamp(reader.readInt(), 0, BendPoint.LAST_POSITION);
        int quarterTones = quarterTonesOf(reader.readInt());
        int vibrato = Math.clamp(reader.readUnsignedByte(), 0, BendPoint.MAX_VIBRATO);
        return new BendPoint(position, quarterTones, vibrato);
    }

    private static int quarterTonesOf(int units) {
        return Math.clamp(
                Math.round(units / (float) UNITS_PER_QUARTER_TONE),
                -BendPoint.MAX_QUARTER_TONES, BendPoint.MAX_QUARTER_TONES);
    }

    /** Codes 1 to 5 belong to the bend; 6 to 11 are specific to the tremolo bar. */
    private static BendType bendTypeOf(int rawType) {
        return switch (rawType) {
            case 1 -> BendType.BEND;
            case 2 -> BendType.BEND_RELEASE;
            case 3 -> BendType.BEND_RELEASE_BEND;
            case 4 -> BendType.PREBEND;
            case 5 -> BendType.PREBEND_RELEASE;
            case 6 -> BendType.DIP;
            case 7 -> BendType.DIVE;
            case 8 -> BendType.RELEASE_UP;
            case 9 -> BendType.INVERTED_DIP;
            case 10 -> BendType.RETURN;
            case 11 -> BendType.RELEASE_DOWN;
            default -> BendType.BEND;
        };
    }
}
