package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * El patch de instrumentos del manual: un archivo de texto que solo cambia
 * los nombres que se muestran, nunca el sonido -"Configure the Sound > MIDI
 * Setup".
 */
class InstrumentPatchTest {

    @Test
    void withoutAPatchEveryProgramKeepsItsGeneralMidiName() {
        InstrumentPatch patch = InstrumentPatch.generalMidi();

        assertEquals(Instruments.nameOf(0), patch.nameOf(0));
        assertEquals(Instruments.nameOf(127), patch.nameOf(127));
    }

    @Test
    void anEmptyFileKeepsEveryGeneralMidiName() {
        InstrumentPatch patch = InstrumentPatch.parse("");

        assertEquals(Instruments.nameOf(0), patch.nameOf(0));
        assertEquals(Instruments.nameOf(64), patch.nameOf(64));
    }

    @Test
    void aSingleLineNamesOnlyProgramZero() {
        InstrumentPatch patch = InstrumentPatch.parse("Requinto criollo");

        assertEquals("Requinto criollo", patch.nameOf(0));
        assertEquals(Instruments.nameOf(1), patch.nameOf(1));
    }

    @Test
    void oneHundredTwentyEightLinesNameEveryProgram() {
        InstrumentPatch patch = InstrumentPatch.parse(oneHundredTwentyEightNames());

        assertEquals("Instrumento 0", patch.nameOf(0));
        assertEquals("Instrumento 127", patch.nameOf(127));
    }

    @Test
    void linesPastProgramOneTwentySevenAreIgnored() {
        StringBuilder text = new StringBuilder(oneHundredTwentyEightNames());
        text.append("\nInstrumento fantasma");

        InstrumentPatch patch = InstrumentPatch.parse(text.toString());

        assertEquals("Instrumento 127", patch.nameOf(127));
        assertThrows(IllegalArgumentException.class, () -> patch.nameOf(128));
    }

    @Test
    void aBlankLineFallsBackToTheGeneralMidiNameForThatProgram() {
        InstrumentPatch patch = InstrumentPatch.parse("Requinto criollo\n\nCharango");

        assertEquals("Requinto criollo", patch.nameOf(0));
        assertEquals(Instruments.nameOf(1), patch.nameOf(1));
        assertEquals("Charango", patch.nameOf(2));
    }

    @Test
    void windowsLineEndingsDoNotLeakIntoTheName() {
        InstrumentPatch patch = InstrumentPatch.parse("Requinto criollo\r\nCharango\r\n");

        assertEquals("Requinto criollo", patch.nameOf(0));
        assertEquals("Charango", patch.nameOf(1));
    }

    @Test
    void rejectsAProgramOutsideTheGeneralMidiSet() {
        InstrumentPatch patch = InstrumentPatch.generalMidi();

        assertThrows(IllegalArgumentException.class, () -> patch.nameOf(-1));
        assertThrows(IllegalArgumentException.class, () -> patch.nameOf(128));
    }

    private static String oneHundredTwentyEightNames() {
        StringBuilder text = new StringBuilder();
        for (int program = 0; program < Instruments.COUNT; program++) {
            if (program > 0) {
                text.append('\n');
            }
            text.append("Instrumento ").append(program);
        }
        return text.toString();
    }
}
