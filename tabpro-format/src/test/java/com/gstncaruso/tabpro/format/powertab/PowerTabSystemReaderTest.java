package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class PowerTabSystemReaderTest {

    private final PowerTabSystemReader reader = new PowerTabSystemReader();

    @Test
    void readsAMinimalSystem() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[16], 0, 16);
        out.write((PowerTabBarline.REPEAT_END << 5) | 4);
        out.write(20);
        out.write(0);
        out.write(0);
        out.write(0);

        writeBarline(out);

        writeEmptyVector(out);
        writeEmptyVector(out);
        writeEmptyVector(out);

        out.write(1);
        out.write(0);
        out.write(0x00);
        out.write(0x00);
        writeStaff(out);

        writeEmptyVector(out);

        PowerTabSystem system = reader.read(new PowerTabByteReader(out.toByteArray()));

        assertEquals(PowerTabBarline.REPEAT_END, system.endBarType());
        assertEquals(4, system.endBarRepeatCount());
        assertEquals(0, system.startBar().position());
        assertEquals(1, system.staves().size());
        assertEquals(0, system.rhythmSlashCount());
        assertTrue(system.internalBarlines().isEmpty());
    }

    private static void writeBarline(ByteArrayOutputStream out) {
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(new byte[4], 0, 4);
        out.write(0);
        out.write(0);
        out.write(0);
    }

    private static void writeStaff(ByteArrayOutputStream out) {
        out.write(0x06);
        out.write(9);
        out.write(9);
        out.write(0);
        out.write(0);
        writeEmptyVector(out);
        writeEmptyVector(out);
    }

    private static void writeEmptyVector(ByteArrayOutputStream out) {
        out.write(0);
        out.write(0);
    }
}
