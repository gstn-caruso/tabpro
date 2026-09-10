package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class PowerTabStaffReaderTest {

    private final PowerTabStaffReader reader = new PowerTabStaffReader();

    @Test
    void readsTheStringCountAndBothVoices() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x06);
        out.write(9);
        out.write(9);
        out.write(0);
        out.write(0);

        out.write(1);
        out.write(0);
        out.write(0x00);
        out.write(0x00);
        writeRestPosition(out, 0);

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
        out.write(0);
        int data = (4 << 24) | 0x04;
        out.write(data & 0xFF);
        out.write((data >>> 8) & 0xFF);
        out.write((data >>> 16) & 0xFF);
        out.write((data >>> 24) & 0xFF);
        out.write(0);
        out.write(0);
        out.write(0);
    }
}
