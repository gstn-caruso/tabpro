package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import org.junit.jupiter.api.Test;

/**
 * La armadura de la cabecera es el entero de alteraciones y nada mas: meterle el modo
 * en el segundo byte inventa un valor que ningun Guitar Pro reconoce.
 */
class GuitarProHeaderWriterTest {

    /** La armadura son los cuatro bytes previos a la octava, que cierra la cabecera. */
    private static final int OCTAVE_BYTES = 1;
    private static final int KEY_BYTES = 4;

    private final GuitarProHeaderWriter writer = new GuitarProHeaderWriter();

    @Test
    void unaArmaduraMenorSeEscribeSoloConSusAlteraciones() {
        assertEquals(-3, keyWritten(new KeySignature(-3, Mode.MINOR)));
    }

    @Test
    void unaArmaduraMayorTambien() {
        assertEquals(4, keyWritten(new KeySignature(4, Mode.MAJOR)));
    }

    private int keyWritten(KeySignature keySignature) {
        GuitarProByteWriter bytes = new GuitarProByteWriter();
        writer.write(bytes, ScoreInfo.empty(), TripletFeel.NONE, Lyrics.none(), 120, keySignature);
        byte[] written = bytes.bytes();
        GuitarProByteReader reader = new GuitarProByteReader(written);
        reader.skip(written.length - KEY_BYTES - OCTAVE_BYTES);
        return reader.readInt();
    }
}
