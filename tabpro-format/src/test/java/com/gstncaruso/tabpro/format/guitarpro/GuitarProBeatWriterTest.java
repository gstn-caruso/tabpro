package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import org.junit.jupiter.api.Test;

/**
 * El byte de estado del beat y la mascara de cuerdas, tal como los pide el formato:
 * vacio es 0, normal es 1 y silencio es 2, y la mascara va siempre, aunque este en cero.
 */
class GuitarProBeatWriterTest {

    private static final int HAS_STATUS = 0x40;
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_REST = 2;
    private static final int QUARTER = 0;
    private static final int NO_STRINGS = 0x00;
    private static final int ONLY_FIRST_STRING = 0x40;

    private final GuitarProBeatWriter writer = new GuitarProBeatWriter();

    @Test
    void unaNotaNormalNoEsUnCompasVacio() {
        byte[] bytes = write(Beat.of(Duration.quarter(), new Note(1, 5)));

        assertEquals(HAS_STATUS, bytes[0] & 0xFF);
        assertEquals(STATUS_NORMAL, bytes[1] & 0xFF);
        assertEquals(QUARTER, bytes[2]);
        assertEquals(ONLY_FIRST_STRING, bytes[3] & 0xFF);
    }

    @Test
    void elSilencioTambienEscribeSuMascaraDeCuerdas() {
        byte[] bytes = write(Beat.rest(Duration.quarter()));

        assertEquals(STATUS_REST, bytes[1] & 0xFF);
        assertEquals(QUARTER, bytes[2]);
        assertEquals(NO_STRINGS, bytes[3] & 0xFF);
        assertEquals(4, bytes.length, "un silencio son exactamente cuatro bytes");
    }

    private byte[] write(Beat beat) {
        GuitarProByteWriter bytes = new GuitarProByteWriter();
        writer.write(bytes, beat);
        return bytes.bytes();
    }
}
