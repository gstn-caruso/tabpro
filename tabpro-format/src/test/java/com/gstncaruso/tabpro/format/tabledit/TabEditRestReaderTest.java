package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.VoicePart;
import org.junit.jupiter.api.Test;

/** El silencio: a diferencia de la nota, su duracion ocupa el byte entero, sin mezclarse con nada. */
class TabEditRestReaderTest {

    private final TabEditRestReader reader = new TabEditRestReader();

    @Test
    void leeLaDuracionDeUnSilencioEnLaVozPrincipal() {
        TabEditRestFields fields = reader.read(new TabEditByteReader(rest(6, false, false)));

        assertEquals(NoteValue.QUARTER, fields.duration().value());
        assertEquals(VoicePart.LEAD, fields.voice());
    }

    @Test
    void unSilencioDeLaVozSecundaria() {
        TabEditRestFields fields = reader.read(new TabEditByteReader(rest(9, true, true)));

        assertEquals(NoteValue.EIGHTH, fields.duration().value());
        assertEquals(VoicePart.BASS, fields.voice());
    }

    private static byte[] rest(int duration, boolean bit5, boolean bit4) {
        int byte2 = (bit5 ? (1 << 5) : 0) | (bit4 ? (1 << 4) : 0);
        return new byte[] {(byte) duration, (byte) byte2, 0, 0, 0, 0, 0};
    }
}
