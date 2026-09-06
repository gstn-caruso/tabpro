package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class PowerTabGuitarInReaderTest {

    private final PowerTabGuitarInReader reader = new PowerTabGuitarInReader();

    @Test
    void matchesIdentityWhenTheStaffPlaysItsOwnGuitar() {
        PowerTabGuitarIn guitarIn = read(1, 0x02); // pentagrama 1, guitarra 1 (bit 1).

        assertTrue(guitarIn.matchesIdentity());
    }

    @Test
    void doesNotMatchIdentityWhenAnotherGuitarIsAssigned() {
        PowerTabGuitarIn guitarIn = read(1, 0x01); // pentagrama 1, pero guitarra 0.

        assertFalse(guitarIn.matchesIdentity());
    }

    private PowerTabGuitarIn read(int staff, int staffGuitarsByte) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0); // sistema.
        out.write(0);
        out.write(staff);
        out.write(0); // posicion.
        out.write(staffGuitarsByte); // dato: byte bajo = guitarras del pentagrama.
        out.write(0); // byte alto = guitarras de rhythm slash, sin uso aqui.
        return reader.read(new PowerTabByteReader(out.toByteArray()));
    }
}
