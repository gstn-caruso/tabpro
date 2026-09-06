package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/**
 * El dato de 16 bits guarda, en el byte alto, la mascara de guitarras del
 * pentagrama; en el byte bajo, la de rhythm slash (guitarin.cpp: GetStaffGuitars
 * devuelve HIBYTE, GetRhythmSlashGuitars devuelve LOBYTE).
 */
class PowerTabGuitarInReaderTest {

    private final PowerTabGuitarInReader reader = new PowerTabGuitarInReader();

    @Test
    void readsTheStaffGuitarsMaskFromTheHighByte() {
        PowerTabGuitarIn guitarIn = read(1, 0x02); // pentagrama 1, guitarra 1 (bit 1).

        assertEquals(1, guitarIn.staff());
        assertEquals(0x02, guitarIn.staffGuitarsMask());
    }

    private PowerTabGuitarIn read(int staff, int staffGuitarsByte) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0); // sistema.
        out.write(0);
        out.write(staff);
        out.write(0); // posicion.
        out.write(0); // byte bajo = guitarras de rhythm slash, sin uso aqui.
        out.write(staffGuitarsByte); // byte alto = guitarras del pentagrama.
        return reader.read(new PowerTabByteReader(out.toByteArray()));
    }
}
