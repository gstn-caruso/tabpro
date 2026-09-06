package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DrumKitsTest {

    @Test
    void listsTheNineGeneralMidiKitsInProgramOrder() {
        assertEquals(
                List.of("Standard", "Room", "Power", "Electronic", "TR-808", "Jazz", "Brush", "Orchestra", "SFX"),
                DrumKits.names());
    }

    @Test
    void theStandardKitIsProgramZero() {
        assertEquals(0, DrumKits.programAt(0));
    }

    @Test
    void theLastKitIsSfx() {
        assertEquals(56, DrumKits.programAt(DrumKits.names().size() - 1));
    }

    @Test
    void eachNameCorrespondsToItsGeneralMidiProgramNumber() {
        assertEquals(0, DrumKits.programAt(0));
        assertEquals(8, DrumKits.programAt(1));
        assertEquals(16, DrumKits.programAt(2));
        assertEquals(24, DrumKits.programAt(3));
        assertEquals(25, DrumKits.programAt(4));
        assertEquals(32, DrumKits.programAt(5));
        assertEquals(40, DrumKits.programAt(6));
        assertEquals(48, DrumKits.programAt(7));
        assertEquals(56, DrumKits.programAt(8));
    }

    @Test
    void findsTheListPositionOfAKnownProgram() {
        assertEquals(4, DrumKits.indexOf(25));
    }

    @Test
    void anUnknownProgramFallsBackToTheStandardKit() {
        assertEquals(0, DrumKits.indexOf(5));
    }
}
