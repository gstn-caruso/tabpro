package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class InstrumentIconTest {

    @Test
    void recognisesTheFamilyOfTheDefaultTracks() {
        assertEquals(InstrumentIcon.Family.GUITAR, InstrumentIcon.familyOf(Track.GUITAR_PROGRAM));
        assertEquals(InstrumentIcon.Family.BASS, InstrumentIcon.familyOf(Track.BASS_PROGRAM));
    }

    @Test
    void recognisesTheOtherFamilies() {
        assertEquals(InstrumentIcon.Family.KEYS, InstrumentIcon.familyOf(0));
        assertEquals(InstrumentIcon.Family.STRINGS, InstrumentIcon.familyOf(40));
        assertEquals(InstrumentIcon.Family.WIND, InstrumentIcon.familyOf(56));
        assertEquals(InstrumentIcon.Family.WIND, InstrumentIcon.familyOf(73));
        assertEquals(InstrumentIcon.Family.DRUMS, InstrumentIcon.familyOf(115));
        assertEquals(InstrumentIcon.Family.OTHER, InstrumentIcon.familyOf(127));
    }

    @Test
    void hasAFamilyForEveryGeneralMidiProgram() {
        for (int program = 0; program < 128; program++) {
            assertTrue(InstrumentIcon.familyOf(program) != null, "falta la familia del programa " + program);
        }
    }

    @Test
    void rejectsAProgramOutsideTheSet() {
        assertThrows(IllegalArgumentException.class, () -> InstrumentIcon.familyOf(128));
        assertThrows(IllegalArgumentException.class, () -> InstrumentIcon.familyOf(-1));
    }

    @Test
    void drawsSomethingForEveryProgram() {
        for (int program = 0; program < 128; program++) {
            BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            InstrumentIcon.paint(g, program, Color.WHITE, 0, 0, 20);
            g.dispose();
            assertTrue(hasAnyPixel(image), "el programa " + program + " no dibujo nada");
        }
    }

    private boolean hasAnyPixel(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
