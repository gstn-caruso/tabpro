package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/**
 * Bytes armados a mano siguiendo barline.cpp/keysignature.cpp/timesignature.cpp
 * de powertabeditor. Los valores replican el barlines.ptb real del proyecto: doble
 * barra, la menor con 2 sostenidos, medida 5/8.
 */
class PowerTabBarlineReaderTest {

    private final PowerTabBarlineReader reader = new PowerTabBarlineReader();

    @Test
    void readsADoubleBarWithAMinorKeyAndAnOddMeter() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(3); // posicion.
        out.write((PowerTabBarline.DOUBLE_BAR << 5)); // tipo, sin repeticion.

        out.write(0x40 | 2); // armadura: menor (bit6), 2 sostenidos.

        // medida: 5 tiempos (zero-based 4, bits 27-31) de octava (exponente 3, bits 24-26).
        int timeData = (4 << 27) | (3 << 24);
        writeInt(out, timeData);
        out.write(5); // pulsos por compas: se descarta.

        out.write('A'); // letra de la marca de ensayo: se descarta.
        writeMfcString(out, "Intro"); // descripcion: se descarta.

        PowerTabBarline barline = reader.read(new PowerTabByteReader(out.toByteArray()));

        assertEquals(3, barline.position());
        assertEquals(PowerTabBarline.DOUBLE_BAR, barline.type());
        assertEquals(Mode.MINOR, barline.keySignature().mode());
        assertEquals(2, barline.keySignature().accidentals());
        assertEquals(5, barline.timeSignature().beats());
        assertEquals(8, barline.timeSignature().beatUnit());
    }

    @Test
    void readsARepeatEndWithItsCount() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0);
        out.write((PowerTabBarline.REPEAT_END << 5) | 3); // tipo repeatEnd, repite 3 veces.
        out.write(0); // armadura de Do mayor.
        writeInt(out, 0); // medida: nada marcado (comun/corte apagados, todo en cero).
        out.write(0);
        out.write(0);
        writeMfcString(out, "");

        PowerTabBarline barline = reader.read(new PowerTabByteReader(out.toByteArray()));

        assertEquals(PowerTabBarline.REPEAT_END, barline.type());
        assertEquals(3, barline.repeatCount());
        assertTrue(barline.isRepeatEnd());
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 24) & 0xFF);
    }

    private static void writeMfcString(ByteArrayOutputStream out, String text) {
        out.write(text.length());
        out.writeBytes(text.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }

}
