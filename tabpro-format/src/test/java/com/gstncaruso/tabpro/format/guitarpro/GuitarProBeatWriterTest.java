package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
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

    /** Guitar Pro escribe en -1 el parametro que el cambio no toca. */
    private static final int UNSET = -1;

    /**
     * Donde arranca el cambio de parametros: banderas, estado, figura y despues el
     * instrumento, que es el primero de sus valores.
     */
    private static final int MIX_TABLE_AT = 3;

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

    /**
     * El archivo espera el volumen y el paneo del cambio de parametros en los dieciseis
     * pasos de la perilla, igual que en la tabla de canales: escribir el valor de MIDI
     * tal cual deja un cambio que ningun Guitar Pro entiende.
     */
    @Test
    void elCambioDeParametrosEscribeLasPerillasEnSusPasos() {
        ParameterChange change = ParameterChange.nothing()
                .changing(SoundParameter.VOLUME, 104)
                .changing(SoundParameter.PAN, 64);
        byte[] bytes = write(Beat.rest(Duration.quarter())
                .withEffects(BeatEffects.none().withParameterChange(change)));

        assertEquals(UNSET, bytes[MIX_TABLE_AT], "el instrumento no cambia");
        assertEquals(13, bytes[MIX_TABLE_AT + 1]);
        assertEquals(8, bytes[MIX_TABLE_AT + 2]);
    }

    private byte[] write(Beat beat) {
        GuitarProByteWriter bytes = new GuitarProByteWriter();
        writer.write(bytes, beat);
        return bytes.bytes();
    }
}
