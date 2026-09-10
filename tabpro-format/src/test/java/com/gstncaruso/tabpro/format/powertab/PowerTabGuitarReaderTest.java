package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

class PowerTabGuitarReaderTest {

    private final PowerTabGuitarReader reader = new PowerTabGuitarReader();

    @Test
    void readsTheDescriptionTheChannelAndTheTuning() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0);
        writeMfcString(out, "Lead Guitar");
        out.write(25);
        out.write(104);
        out.write(64);
        out.write(10);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(3);
        writeMfcString(out, "Standard");
        out.write(0);
        int[] notes = {64, 59, 55, 50, 45, 40};
        out.write(notes.length);
        for (int note : notes) {
            out.write(note);
        }

        PowerTabGuitar guitar = reader.read(new PowerTabByteReader(out.toByteArray()));

        assertEquals("Lead Guitar", guitar.description());
        assertEquals(25, guitar.preset());
        assertEquals(104, guitar.initialVolume());
        assertEquals(64, guitar.pan());
        assertEquals(10, guitar.reverb());
        assertEquals(3, guitar.capo());
        assertEquals(List.of(64, 59, 55, 50, 45, 40), guitar.tuningMidiNotes());
    }

    private static void writeMfcString(ByteArrayOutputStream out, String text) {
        out.write(text.length());
        out.writeBytes(text.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }
}
