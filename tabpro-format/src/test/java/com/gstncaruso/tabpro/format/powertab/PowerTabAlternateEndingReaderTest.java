package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Replica los valores del alternate_endings.ptb real: vueltas 2a y 3a, y D.C. (que se ignora). */
class PowerTabAlternateEndingReaderTest {

    private final PowerTabAlternateEndingReader reader = new PowerTabAlternateEndingReader();

    @Test
    void readsTheNumbersAndIgnoresDaCapo() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0); // sistema.
        out.write(0);
        out.write(5); // posicion.
        int numbersMask = (1 << 1) | (1 << 2) | (1 << 8); // vueltas 2 y 3, mas D.C. (numero 9).
        int data = numbersMask << 16;
        out.write(data & 0xFF);
        out.write((data >>> 8) & 0xFF);
        out.write((data >>> 16) & 0xFF);
        out.write((data >>> 24) & 0xFF);

        PowerTabAlternateEnding ending = reader.read(new PowerTabByteReader(out.toByteArray()));

        assertEquals(5, ending.position());
        assertEquals(List.of(2, 3), ending.numbers());
    }
}
