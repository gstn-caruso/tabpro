package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChosenScaleTest {

    private final ChosenScale chosen = new ChosenScale();

    @Test
    void startsWithoutAnyScaleChosen() {
        assertTrue(chosen.scale().isEmpty());
        assertTrue(chosen.tones().isEmpty());
        assertEquals(List.of(), chosen.semitonesFromTheTonic());
    }

    @Test
    void aMajorScaleGivesTheSevenStepsOfTheMajorPattern() {
        chosen.choose(PitchClass.of("C"), major());

        assertEquals(List.of(0, 2, 4, 5, 7, 9, 11), chosen.semitonesFromTheTonic());
    }

    @Test
    void thePatternDoesNotChangeWhenTheTonicChanges() {
        chosen.choose(PitchClass.of("A"), major());

        assertEquals(List.of(0, 2, 4, 5, 7, 9, 11), chosen.semitonesFromTheTonic());
    }

    @Test
    void forgettingTheScaleLeavesNothingToDraw() {
        chosen.choose(PitchClass.of("C"), major());

        chosen.forget();

        assertTrue(chosen.scale().isEmpty());
        assertEquals(List.of(), chosen.semitonesFromTheTonic());
    }

    private static Scale major() {
        return ScaleLibrary.all().stream()
                .filter(scale -> scale.name().toLowerCase(java.util.Locale.ROOT).startsWith("mayor"))
                .findFirst()
                .orElseThrow();
    }
}
