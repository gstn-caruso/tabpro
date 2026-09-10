package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import org.junit.jupiter.api.Test;

class HarmonicPitchTest {

    private static final Pitch OPEN_STRING = new Pitch(40);

    @Test
    void theNaturalHarmonicAtFret12IsAnOctaveAboveTheOpenString() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(52), 12);

        assertEquals(new Pitch(52), pitch);
    }

    @Test
    void theNaturalHarmonicAtFret7IsATwelfthAboveTheOpenString() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(47), 7);

        assertEquals(new Pitch(59), pitch);
    }

    @Test
    void theNaturalHarmonicAtFret19IsTheSameNodeAsFret7() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(59), 19);

        assertEquals(new Pitch(59), pitch);
    }

    @Test
    void theNaturalHarmonicAtFret5IsTwoOctaves() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(45), 5);

        assertEquals(new Pitch(64), pitch);
    }

    @Test
    void theNaturalHarmonicAtFret4IsTwoOctavesAndAThird() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(44), 4);

        assertEquals(new Pitch(68), pitch);
    }

    @Test
    void aFretWithoutAKnownNodeSoundsLikeTheFrettedNote() {
        Pitch fretted = new Pitch(43);
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, fretted, 3);

        assertEquals(fretted, pitch);
    }

    @Test
    void theArtificialHarmonicSoundsAnOctaveAboveTheFrettedNote() {
        Pitch fretted = new Pitch(50);
        Pitch pitch = HarmonicPitch.of(HarmonicType.ARTIFICIAL, OPEN_STRING, fretted, 10);

        assertEquals(new Pitch(62), pitch);
    }

    @Test
    void thePinchHarmonicAlsoTransposesAnOctave() {
        Pitch fretted = new Pitch(55);
        Pitch pitch = HarmonicPitch.of(HarmonicType.PINCH, OPEN_STRING, fretted, 15);

        assertEquals(new Pitch(67), pitch);
    }
}
