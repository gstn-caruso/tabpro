package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import org.junit.jupiter.api.Test;

/**
 * La escala de la curva la fija el formato, no tabpro: Guitar Pro guarda 25 unidades
 * por cuarto de tono, asi que un tono entero (cuatro cuartos) se escribe como 100.
 */
class GuitarProBendReaderTest {

    private static final int WHOLE_TONE = 100;
    private static final int HALF_TONE = 50;
    private static final int BEND = 1;
    private static final int NO_VIBRATO = 0;

    private final GuitarProBendReader reader = new GuitarProBendReader();

    @Test
    void cienUnidadesSonUnTonoEntero() {
        Bend bend = read(new GuitarProFileWriter()
                .writeSignedByte(BEND)
                .writeInt(WHOLE_TONE)
                .writeInt(2)
                .writeInt(0).writeInt(0).writeUnsignedByte(NO_VIBRATO)
                .writeInt(BendPoint.LAST_POSITION).writeInt(WHOLE_TONE).writeUnsignedByte(NO_VIBRATO));

        assertEquals(4, bend.points().get(1).quarterTones());
    }

    @Test
    void cincuentaUnidadesSonMedioTono() {
        Bend bend = read(new GuitarProFileWriter()
                .writeSignedByte(BEND)
                .writeInt(HALF_TONE)
                .writeInt(2)
                .writeInt(0).writeInt(0).writeUnsignedByte(NO_VIBRATO)
                .writeInt(BendPoint.LAST_POSITION).writeInt(HALF_TONE).writeUnsignedByte(NO_VIBRATO));

        assertEquals(2, bend.points().get(1).quarterTones());
    }

    private Bend read(GuitarProFileWriter written) {
        return reader.read(new GuitarProByteReader(written.bytes()));
    }
}
