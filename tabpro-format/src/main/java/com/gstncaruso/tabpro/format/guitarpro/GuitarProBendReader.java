package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee la curva de un bend o de una palanca. Guitar Pro guarda la posicion de
 * 0 a 60 (igual que {@link BendPoint#LAST_POSITION}) y la profundidad en unidades
 * de las que entran 25 en cada cuarto de tono: un tono entero, que es el bend
 * completo, se escribe como 100. Tabpro la guarda en cuartos de tono.
 */
final class GuitarProBendReader {

    private static final int UNITS_PER_QUARTER_TONE = 25;

    Bend read(GuitarProByteReader reader) {
        int rawType = reader.readSignedByte();
        reader.readInt(); // profundidad general de la curva: se puede derivar de los puntos.
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
     * La palanca de GP3 no tiene curva: es un solo entero con cuanto se hunde la cuerda.
     * Se le da la forma con que la dibuja Guitar Pro -- baja hasta la mitad de la nota y
     * vuelve a su altura.
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

    /**
     * Los primeros cinco tipos son los que tabpro reconoce; los que usa la
     * palanca (dip, dive, etc.) no tienen equivalente exacto y se aproximan
     * al mas parecido.
     */
    private static BendType bendTypeOf(int rawType) {
        return switch (rawType) {
            case 1 -> BendType.BEND;
            case 2 -> BendType.BEND_RELEASE;
            case 3 -> BendType.BEND_RELEASE_BEND;
            case 4 -> BendType.PREBEND;
            case 5 -> BendType.PREBEND_RELEASE;
            case 8 -> BendType.BEND_RELEASE;
            case 9 -> BendType.PREBEND;
            case 10, 11 -> BendType.BEND_RELEASE;
            default -> BendType.BEND;
        };
    }
}
