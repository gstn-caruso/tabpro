package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScaleTest {

    @Test
    void cMajorHasTheWhiteKeysOfItsOctave() {
        Scale scale = Scale.cMajor();

        for (int midi : new int[] {60, 62, 64, 65, 67, 69, 71}) {
            assertTrue(scale.contains(midi), midi + " es de Do mayor");
        }
        for (int midi : new int[] {61, 63, 66, 68, 70}) {
            assertFalse(scale.contains(midi), midi + " no es de Do mayor");
        }
    }

    @Test
    void repeatsTheSamePatternEveryOctave() {
        Scale scale = Scale.cMajor();

        assertTrue(scale.contains(48));
        assertTrue(scale.contains(72));
        assertFalse(scale.contains(49));
    }

    @Test
    void shiftsWithARootOtherThanC() {
        Scale dMajor = new Scale(2, ScaleType.MAJOR);

        for (int midi : new int[] {62, 64, 66, 67, 69, 71, 73}) {
            assertTrue(dMajor.contains(midi), midi + " es de Re mayor");
        }
        assertFalse(dMajor.contains(65), "fa natural no es de Re mayor");
    }

    @Test
    void aPentatonicScaleHasFiveNotesPerOctave() {
        Scale scale = new Scale(0, ScaleType.MINOR_PENTATONIC);

        int found = 0;
        for (int midi = 60; midi < 72; midi++) {
            if (scale.contains(midi)) {
                found++;
            }
        }
        assertTrue(found == 5, "la pentatonica tiene cinco notas por octava, encontre " + found);
    }

    @Test
    void rejectsAPitchClassOutsideAnOctave() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> new Scale(12, ScaleType.MAJOR));
    }
}
