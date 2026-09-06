package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/** Bytes armados a mano siguiendo staff.cpp/position.cpp de powertabeditor. */
class PowerTabStaffReaderTest {

    private final PowerTabStaffReader reader = new PowerTabStaffReader();

    @Test
    void readsTheStringCountAndBothVoices() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x06); // clave treble (bits altos en 0) + 6 cuerdas.
        out.write(9); // espaciados de dibujo, sin uso.
        out.write(9);
        out.write(0);
        out.write(0);

        // voz principal: una posicion, un silencio de negra.
        out.write(1); // conteo (word bajo).
        out.write(0);
        out.write(0x00); // etiqueta de clase: referencia corta, sin esquema.
        out.write(0x00);
        writeRestPosition(out, 0);

        // segunda voz: vacia.
        out.write(0);
        out.write(0);

        PowerTabStaff staff = reader.read(new PowerTabByteReader(out.toByteArray()));

        assertEquals(6, staff.stringCount());
        assertEquals(1, staff.voices().get(0).size());
        assertTrue(staff.voices().get(1).isEmpty());
        assertTrue(staff.voices().get(0).get(0).beat().isRest());
    }

    private static void writeRestPosition(ByteArrayOutputStream out, int index) {
        out.write(index);
        out.write(0);
        out.write(0); // beaming.
        int data = (4 << 24) | 0x04; // negra, silencio.
        out.write(data & 0xFF);
        out.write((data >>> 8) & 0xFF);
        out.write((data >>> 16) & 0xFF);
        out.write((data >>> 24) & 0xFF);
        out.write(0); // sin simbolos complejos.
        out.write(0); // sin notas (word).
        out.write(0);
    }
}
