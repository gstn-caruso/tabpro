package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class GuitarProBeatReaderTest {

    private static final int NO_FLAGS = 0x00;
    private static final int DOTTED = 0x01;
    private static final int WITH_TUPLET = 0x20;
    private static final int WITH_MIX_TABLE = 0x10;
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

    /** Guitar Pro escribe en -1 el parametro que el cambio no toca. */
    private static final int UNCHANGED = -1;
    private static final int NO_STRINGS = 0x00;
    private static final int NORMAL_NOTE = 1;
    private static final int TIED_NOTE = 2;
    private static final int DEAD_NOTE = 3;

    private final GuitarProBeatReader reader = new GuitarProBeatReader();

    @Test
    void aRestHasNoNotes() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_STATUS)
                .writeUnsignedByte(REST_STATUS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_STRINGS));

        assertTrue(beat.isRest());
        assertEquals(NoteValue.QUARTER, beat.duration().value());
    }

    /**
     * El formato escribe la mascara de cuerdas en todo beat, tambien en el silencio.
     * Si el lector no la consume, se corre un byte y arruina todo lo que sigue.
     */
    @Test
    void unSilencioIgualTraeSuMascaraDeCuerdas() {
        GuitarProByteReader bytes = new GuitarProByteReader(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_STATUS)
                .writeUnsignedByte(REST_STATUS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_STRINGS)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .bytes());

        Beat silencio = reader.read(bytes, GuitarProVersion.GP4, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP4, 6);

        assertTrue(silencio.isRest());
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
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

    @Test
    void aMixTableChangeBecomesAParameterChange() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(30)
                .writeSignedByte(40)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(90)
                .writeSignedByte(2)
                .writeSignedByte(4)
                .writeUnsignedByte(NO_STRINGS));

        ParameterChange change = beat.effects().parameterChange();
        assertEquals(OptionalInt.of(30), change.valueOf(SoundParameter.PROGRAM));
        assertEquals(OptionalInt.of(40), change.valueOf(SoundParameter.VOLUME));
        assertEquals(OptionalInt.of(90), change.valueOf(SoundParameter.TEMPO));
        assertFalse(change.changes(SoundParameter.PAN), "lo que viene en -1 no cambia");
    }

    @Test
    void theLongestOfTheTransitionsIsTheOneThatCounts() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(40)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(90)
                .writeSignedByte(2)
                .writeSignedByte(4)
                .writeUnsignedByte(NO_STRINGS));

        assertEquals(4, beat.effects().parameterChange().transitionBeats());
    }

    @Test
    void aBeatWithoutAMixTableChangeChangesNothing() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_STRINGS));

        assertTrue(beat.effects().parameterChange().isEmpty());
    }

    @Test
    void theMaskOfTracksDecidesIfTheChangeIsForEveryTrack() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(40)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(UNCHANGED)
                .writeSignedByte(0)
                .writeUnsignedByte(0x01)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertTrue(beat.effects().parameterChange().everyTrack());
    }

    // ---- el wah del cambio de parametros, que existe recien en GP5 -----------

    @Test
    void gp3AndGp4NeverBringAWah() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(UNCHANGED)
                .writeUnsignedByte(0x00)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertTrue(beat.effects().wah().isEmpty());
    }

    @Test
    void minusTwoTurnsTheWahOff() {
        Beat beat = read(gp5Beat(-2, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.OFF, beat.effects().wah().orElseThrow());
    }

    @Test
    void zeroIsAClosedWah() {
        Beat beat = read(gp5Beat(0, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.CLOSED, beat.effects().wah().orElseThrow());
    }

    @Test
    void oneHundredIsAnOpenWah() {
        Beat beat = read(gp5Beat(100, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.OPEN, beat.effects().wah().orElseThrow());
    }

    /** El pedal a mitad de camino redondea al mas cercano de los dos extremos que tabpro distingue. */
    @Test
    void justBelowHalfwayIsStillClosed() {
        Beat beat = read(gp5Beat(49, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.CLOSED, beat.effects().wah().orElseThrow());
    }

    @Test
    void halfwayIsAlreadyOpen() {
        Beat beat = read(gp5Beat(50, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.OPEN, beat.effects().wah().orElseThrow());
    }

    /** -1 es que este cambio de parametros no toca el wah, como el resto de los parametros. */
    @Test
    void unchangedWahIsNotBroughtAtAll() {
        Beat beat = read(gp5Beat(UNCHANGED, true), GuitarProVersion.GP5_10);

        assertTrue(beat.effects().wah().isEmpty());
    }

    /** En 5.00 el cambio de parametros no trae nombre ni categoria del efecto de RSE. */
    @Test
    void gp500HasNoRseInstrumentEffectAfterTheWah() {
        GuitarProByteReader bytes = new GuitarProByteReader(gp5Beat(100, false)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .writeUnsignedByte(NO_FLAGS)
                .writeShort(0)
                .bytes());

        Beat wahBeat = reader.read(bytes, GuitarProVersion.GP5_00, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP5_00, 6);

        assertEquals(Wah.OPEN, wahBeat.effects().wah().orElseThrow());
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    /** En 5.10 el cambio de parametros agrega nombre y categoria del efecto de RSE tras el wah. */
    @Test
    void gp510KeepsTheAlignmentAfterTheRseInstrumentEffect() {
        GuitarProByteReader bytes = new GuitarProByteReader(gp5Beat(100, true)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .writeUnsignedByte(NO_FLAGS)
                .writeShort(0)
                .bytes());

        Beat wahBeat = reader.read(bytes, GuitarProVersion.GP5_10, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP5_10, 6);

        assertEquals(Wah.OPEN, wahBeat.effects().wah().orElseThrow());
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    /**
     * Un beat completo de GP5, sin notas, con un cambio de parametros que no toca nada salvo
     * el wah pedido: instrumento, volumen, pan, chorus, reverb, phaser, tremolo y tempo en -1
     * (sin bytes de transicion), la mascara de "para todas las pistas" en cero. Si el archivo
     * es 5.10, agrega nombre y categoria de efecto de RSE vacios, que solo esa version trae,
     * y siempre cierra con la mascara de cuerdas (sin notas) y el final de beat que GP5 agrega
     * a todos (la segunda voz).
     */
    private static GuitarProFileWriter gp5Beat(int wah, boolean withRseInstrumentEffect) {
        GuitarProFileWriter writer = new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED);
        for (int i = 0; i < 16; i++) {
            writer.writeUnsignedByte(0);
        }
        writer.writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeLengthPrefixedString("")
                .writeInt(UNCHANGED)
                .writeUnsignedByte(0x00)
                .writeSignedByte(wah);
        if (withRseInstrumentEffect) {
            writer.writeLengthPrefixedString("").writeLengthPrefixedString("");
        }
        return writer.writeUnsignedByte(NO_STRINGS).writeShort(0);
    }

    private Beat read(GuitarProFileWriter written) {
        return read(written, GuitarProVersion.GP3);
    }

    private Beat read(GuitarProFileWriter written, GuitarProVersion version) {
        return reader.read(new GuitarProByteReader(written.bytes()), version, 6);
    }
}
