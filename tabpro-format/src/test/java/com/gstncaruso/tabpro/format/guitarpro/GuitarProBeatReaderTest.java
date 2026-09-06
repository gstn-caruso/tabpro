package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;
import org.junit.jupiter.api.Test;

class GuitarProBeatReaderTest {

    private static final int NO_FLAGS = 0x00;
    private static final int DOTTED = 0x01;
    private static final int WITH_TUPLET = 0x20;
    private static final int WITH_STATUS = 0x40;
    private static final int REST_STATUS = 0x02;

    private static final int QUARTER = 0;
    private static final int EIGHTH = 1;
    private static final int WHOLE = -2;

    /** La cuerda 1 ocupa el bit mas alto de la mascara. */
    private static final int ONLY_FIRST_STRING = 0x40;
    private static final int TWO_HIGHEST_STRINGS = 0x60;

    /** El unico dato que trae una nota normal es su tipo y su traste. */
    private static final int NOTE_WITH_FRET = 0x20;
    private static final int NORMAL_NOTE = 1;
    private static final int TIED_NOTE = 2;
    private static final int DEAD_NOTE = 3;

    private final GuitarProBeatReader reader = new GuitarProBeatReader();

    @Test
    void aRestHasNoNotes() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_STATUS)
                .writeUnsignedByte(REST_STATUS)
                .writeSignedByte(QUARTER));

        assertTrue(beat.isRest());
        assertEquals(NoteValue.QUARTER, beat.duration().value());
    }

    @Test
    void aDottedFigureIsMarkedInTheFlags() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(DOTTED)
                .writeSignedByte(EIGHTH)
                .writeUnsignedByte(0));

        assertEquals(NoteValue.EIGHTH, beat.duration().value());
        assertTrue(beat.duration().dotted());
    }

    @Test
    void aWholeNoteIsWrittenAsMinusTwo() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(WHOLE)
                .writeUnsignedByte(0));

        assertEquals(NoteValue.WHOLE, beat.duration().value());
    }

    @Test
    void aTripletComesAsItsOwnInteger() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_TUPLET)
                .writeSignedByte(EIGHTH)
                .writeInt(3)
                .writeUnsignedByte(0));

        assertEquals(Tuplet.of(3), beat.duration().tuplet());
    }

    @Test
    void anUnknownTupletFallsBackToAPlainFigure() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_TUPLET)
                .writeSignedByte(EIGHTH)
                .writeInt(4)
                .writeUnsignedByte(0));

        assertTrue(beat.duration().tuplet().isPlain());
    }

    @Test
    void theStringMaskSaysWhichStringsSound() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TWO_HIGHEST_STRINGS)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(5)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(7));

        assertEquals(2, beat.notes().size());
        assertEquals(5, beat.noteOn(1).orElseThrow().fret());
        assertEquals(7, beat.noteOn(2).orElseThrow().fret());
    }

    @Test
    void aTiedNoteIsNotPlayedAgain() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(TIED_NOTE).writeSignedByte(3));

        assertTrue(beat.noteOn(1).orElseThrow().tied());
    }

    @Test
    void aDeadNoteCarriesItsOrnament() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(DEAD_NOTE).writeSignedByte(0));

        assertTrue(beat.noteOn(1).orElseThrow()
                .has(com.gstncaruso.tabpro.core.model.effects.Ornament.DEAD));
        assertFalse(beat.isRest());
    }

    private Beat read(GuitarProFileWriter written) {
        return reader.read(new GuitarProByteReader(written.bytes()), GuitarProVersion.GP3, 6);
    }
}
