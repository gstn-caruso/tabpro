package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class PowerTabGuitarInReaderTest {

    private final PowerTabGuitarInReader reader = new PowerTabGuitarInReader();

    @Test
    void readsTheStaffGuitarsMaskFromTheHighByte() {
        PowerTabGuitarIn guitarIn = read(1, 0x02);

        assertEquals(1, guitarIn.staff());
        assertEquals(0x02, guitarIn.staffGuitarsMask());
    }

    private PowerTabGuitarIn read(int staff, int staffGuitarsByte) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0);
        out.write(0);
        out.write(staff);
        out.write(0);
        out.write(0);
        out.write(staffGuitarsByte);
        return reader.read(new PowerTabByteReader(out.toByteArray()));
    }
}
