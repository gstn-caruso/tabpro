package com.gstncaruso.tabpro.ui.percussion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PercussionLineTest {

    @Test
    void usesTheAcousticSoundByDefault() {
        assertEquals(38, PercussionLine.SNARE.soundToUse(false));
        assertEquals(35, PercussionLine.KICK.soundToUse(false));
    }

    @Test
    void switchesToTheElectricSoundWhenAskedAndAvailable() {
        assertEquals(40, PercussionLine.SNARE.soundToUse(true));
        assertEquals(36, PercussionLine.KICK.soundToUse(true));
    }

    @Test
    void fallsBackToTheAcousticSoundWhenThereIsNoElectricAlternative() {
        assertEquals(42, PercussionLine.HI_HAT.soundToUse(true));
        assertFalse(PercussionLine.HI_HAT.hasElectricAlternative());
    }

    @Test
    void everySoundIsPlayableOnEveryBoard() {
        for (PercussionLine line : PercussionLine.values()) {
            assertTrue(
                    com.gstncaruso.tabpro.core.model.PercussionKit.isPlayable(line.soundToUse(false)),
                    line + " acustico");
            assertTrue(
                    com.gstncaruso.tabpro.core.model.PercussionKit.isPlayable(line.soundToUse(true)),
                    line + " electrico");
        }
    }

    @Test
    void eachLineHasItsOwnNumberFromOneToSix() {
        var numbers = new java.util.HashSet<Integer>();
        for (PercussionLine line : PercussionLine.values()) {
            numbers.add(line.number());
        }
        assertEquals(6, numbers.size());
        assertEquals(java.util.Set.of(1, 2, 3, 4, 5, 6), numbers);
    }
}
