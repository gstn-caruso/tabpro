package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

class PowerTabAlternateEndingReaderTest {

    private final PowerTabAlternateEndingReader reader = new PowerTabAlternateEndingReader();

    @Test
    void readsTheNumbersAndIgnoresDaCapo() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0);
        out.write(0);
        out.write(5);
        int numbersMask = (1 << 1) | (1 << 2) | (1 << 8);
        int data = numbersMask << 16;
        out.write(data & 0xFF);
        out.write((data >>> 8) & 0xFF);
        out.write((data >>> 16) & 0xFF);
        out.write((data >>> 24) & 0xFF);

        PowerTabAlternateEnding ending = reader.read(new PowerTabByteReader(out.toByteArray()));

        assertEquals(0, ending.system());
        assertEquals(5, ending.position());
        assertEquals(List.of(2, 3), ending.numbers());
    }
}
