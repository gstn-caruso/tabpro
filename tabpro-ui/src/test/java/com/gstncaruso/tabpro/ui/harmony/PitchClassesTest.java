package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.harmony.PitchClass;
import java.util.List;
import org.junit.jupiter.api.Test;

class PitchClassesTest {

    @Test
    void offersTheTwelveChromaticNotesStartingOnC() {
        List<PitchClass> notes = PitchClasses.chromatic();

        assertEquals(12, notes.size());
        assertEquals(PitchClass.of("C"), notes.get(0));
        assertEquals(0, notes.get(0).semitone());
    }

    @Test
    void eachNoteIsOneSemitoneAboveThePrevious() {
        List<PitchClass> notes = PitchClasses.chromatic();

        for (int i = 0; i < notes.size(); i++) {
            assertEquals(i, notes.get(i).semitone());
        }
    }
}
