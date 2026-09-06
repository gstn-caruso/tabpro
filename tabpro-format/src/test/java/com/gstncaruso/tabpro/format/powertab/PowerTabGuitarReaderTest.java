package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Los bytes se arman a mano siguiendo el layout de guitar.cpp/tuning.cpp de powertabeditor. */
class PowerTabGuitarReaderTest {

    private final PowerTabGuitarReader reader = new PowerTabGuitarReader();

    @Test
    void readsTheDescriptionTheChannelAndTheTuning() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0); // numero.
        writeMfcString(out, "Lead Guitar");
        out.write(25); // preset.
        out.write(104); // volumen inicial.
        out.write(64); // pan.
        out.write(10); // reverb.
        out.write(0); // chorus.
        out.write(0); // tremolo.
        out.write(0); // phaser.
        out.write(3); // cejilla.
        writeMfcString(out, "Standard"); // nombre de la afinacion.
        out.write(0); // sostenidos/bemoles y corrimiento.
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
