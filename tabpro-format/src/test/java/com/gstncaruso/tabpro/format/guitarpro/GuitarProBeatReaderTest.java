package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuitarProBeatReaderTest {

    private static final int NO_FLAGS = 0x00;
    private static final int DOTTED = 0x01;
    private static final int HAS_EFFECTS = 0x08;
    private static final int WITH_TUPLET = 0x20;
    private static final int WITH_MIX_TABLE = 0x10;
    private static final int WITH_STATUS = 0x40;
    private static final int REST_STATUS = 0x02;

    private static final int QUARTER = 0;
    private static final int EIGHTH = 1;
    private static final int WHOLE = -2;

    private static final int ONLY_FIRST_STRING = 0x40;
    private static final int TWO_HIGHEST_STRINGS = 0x60;

    private static final int NOTE_WITH_FRET = 0x20;

    private static final int HAS_PICKSTROKE = 0x02;

    private static final int WIDE_VIBRATO = 0x02;

    private static final int HAS_STROKE = 0x40;
    private static final int NO_STROKE = 0;

    private static final int TREMOLO_BAR_OR_SLAP = 0x20;
    private static final int NO_SLAP = 0;
    private static final int SLAPPING = 2;

    private static final int USES_RSE = 0x40;

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

    @Test
    void aRestAlsoCarriesItsStringMask() {
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

        Beat rest = reader.read(bytes, GuitarProVersion.GP4, 6);
        Beat next = reader.read(bytes, GuitarProVersion.GP4, 6);

        assertTrue(rest.isRest());
        assertEquals(9, next.noteOn(1).orElseThrow().fret());
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
                .writeSignedByte(5)
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
    void aMixTableChangeCentersThePanOnTheEighthStep() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(8)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(UNCHANGED)
                .writeSignedByte(0)
                .writeUnsignedByte(NO_STRINGS));

        assertEquals(OptionalInt.of(64),
                beat.effects().parameterChange().valueOf(SoundParameter.PAN));
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

    @Test
    void theTopBitsOfTheMaskAreNotAboutEveryTrack() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(30)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(UNCHANGED)
                .writeUnsignedByte(USES_RSE)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertFalse(beat.effects().parameterChange().everyTrack());
    }

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

    @Test
    void unchangedWahIsNotBroughtAtAll() {
        Beat beat = read(gp5Beat(UNCHANGED, true), GuitarProVersion.GP5_10);

        assertTrue(beat.effects().wah().isEmpty());
    }

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
        Beat next = reader.read(bytes, GuitarProVersion.GP5_00, 6);

        assertEquals(Wah.OPEN, wahBeat.effects().wah().orElseThrow());
        assertEquals(9, next.noteOn(1).orElseThrow().fret());
    }

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
        Beat next = reader.read(bytes, GuitarProVersion.GP5_10, 6);

        assertEquals(Wah.OPEN, wahBeat.effects().wah().orElseThrow());
        assertEquals(9, next.noteOn(1).orElseThrow().fret());
    }

    @ParameterizedTest
    @CsvSource({"1, UP", "2, DOWN"})
    void thePickstrokeSaysWhichWayThePickGoes(int written, PickstrokeDirection expected) {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_FLAGS).writeUnsignedByte(HAS_PICKSTROKE)
                .writeSignedByte(written)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertEquals(expected, beat.effects().pickstroke().orElseThrow());
    }

    @Test
    void aPickstrokeInZeroIsNoPickstrokeAtAll() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_FLAGS).writeUnsignedByte(HAS_PICKSTROKE)
                .writeSignedByte(0)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertTrue(beat.effects().pickstroke().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"GP3", "GP4", "GP5_10"})
    void theWideVibratoIsTheSameBitInEveryGeneration(GuitarProVersion version) {
        GuitarProFileWriter written = new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(WIDE_VIBRATO);
        if (version != GuitarProVersion.GP3) {
            written.writeUnsignedByte(NO_FLAGS);
        }
        written.writeUnsignedByte(NO_STRINGS);
        if (version == GuitarProVersion.GP5_10) {
            written.writeShort(0);
        }

        assertTrue(read(written, version).effects().wideVibrato());
    }

    @ParameterizedTest
    @CsvSource({"1, SIXTY_FOURTH", "2, SIXTY_FOURTH", "3, THIRTY_SECOND",
            "4, SIXTEENTH", "5, EIGHTH", "6, QUARTER"})
    void theStrokeSpeedIsAFigureNumber(int written, NoteValue expected) {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(written)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertEquals(expected, beat.effects().stroke().orElseThrow().speed());
    }

    @Test
    void gp4WritesFirstTheStrokeGoingDown() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(3)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertEquals(StrokeDirection.DOWN, beat.effects().stroke().orElseThrow().direction());
    }

    @Test
    void gp5WritesFirstTheStrokeGoingUp() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(3)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STRINGS)
                .writeShort(0), GuitarProVersion.GP5_10);

        assertEquals(StrokeDirection.UP, beat.effects().stroke().orElseThrow().direction());
    }

    @Test
    void gp5ReadsTheStrokeGoingDownFromTheSecondSpeed() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(3)
                .writeUnsignedByte(NO_STRINGS)
                .writeShort(0), GuitarProVersion.GP5_10);

        assertEquals(StrokeDirection.DOWN, beat.effects().stroke().orElseThrow().direction());
    }

    @Test
    void aBeatWithBothSpeedsInZeroHasNoStroke() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertTrue(beat.effects().stroke().isEmpty());
    }

    @Test
    void gp3ReadsTheTremoloBarAsASingleDepth() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TREMOLO_BAR_OR_SLAP)
                .writeUnsignedByte(NO_SLAP)
                .writeInt(100)
                .writeUnsignedByte(NO_STRINGS));

        Bend tremoloBar = beat.effects().tremoloBar().orElseThrow();
        assertEquals(-4, tremoloBar.points().get(1).quarterTones(), "un tono entero hacia abajo");
        assertEquals(0, tremoloBar.points().getFirst().quarterTones());
        assertEquals(0, tremoloBar.points().getLast().quarterTones());
    }

    @Test
    void gp3KeepsTheAlignmentAfterATremoloBar() {
        GuitarProByteReader bytes = new GuitarProByteReader(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TREMOLO_BAR_OR_SLAP)
                .writeUnsignedByte(NO_SLAP)
                .writeInt(100)
                .writeUnsignedByte(NO_STRINGS)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .bytes());

        reader.read(bytes, GuitarProVersion.GP3, 6);
        Beat next = reader.read(bytes, GuitarProVersion.GP3, 6);

        assertEquals(9, next.noteOn(1).orElseThrow().fret());
    }

    @Test
    void gp3KeepsTheAlignmentAfterASlap() {
        GuitarProByteReader bytes = new GuitarProByteReader(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TREMOLO_BAR_OR_SLAP)
                .writeUnsignedByte(SLAPPING)
                .writeInt(0)
                .writeUnsignedByte(NO_STRINGS)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .bytes());

        Beat slap = reader.read(bytes, GuitarProVersion.GP3, 6);
        Beat next = reader.read(bytes, GuitarProVersion.GP3, 6);

        assertTrue(slap.effects().slapping());
        assertEquals(9, next.noteOn(1).orElseThrow().fret());
    }

    @Test
    void gp4KeepsTheAlignmentAfterASlap() {
        GuitarProByteReader bytes = new GuitarProByteReader(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TREMOLO_BAR_OR_SLAP)
                .writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(SLAPPING)
                .writeUnsignedByte(NO_STRINGS)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .bytes());

        Beat slap = reader.read(bytes, GuitarProVersion.GP4, 6);
        Beat next = reader.read(bytes, GuitarProVersion.GP4, 6);

        assertTrue(slap.effects().slapping());
        assertEquals(9, next.noteOn(1).orElseThrow().fret());
    }

    @Test
    void gp510KeepsTheAlignmentAfterTheHiddenTempoFlag() {
        GuitarProByteReader bytes = new GuitarProByteReader(gp5BeatChangingTempo(140, GuitarProVersion.GP5_10)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .writeUnsignedByte(NO_FLAGS)
                .writeShort(0)
                .bytes());

        Beat tempoBeat = reader.read(bytes, GuitarProVersion.GP5_10, 6);
        Beat next = reader.read(bytes, GuitarProVersion.GP5_10, 6);

        assertEquals(OptionalInt.of(140),
                tempoBeat.effects().parameterChange().valueOf(SoundParameter.TEMPO));
        assertEquals(9, next.noteOn(1).orElseThrow().fret());
    }

    @Test
    void gp500HasNoHiddenTempoFlagAfterTheTempo() {
        GuitarProByteReader bytes = new GuitarProByteReader(gp5BeatChangingTempo(140, GuitarProVersion.GP5_00)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .writeUnsignedByte(NO_FLAGS)
                .writeShort(0)
                .bytes());

        Beat tempoBeat = reader.read(bytes, GuitarProVersion.GP5_00, 6);
        Beat next = reader.read(bytes, GuitarProVersion.GP5_00, 6);

        assertEquals(OptionalInt.of(140),
                tempoBeat.effects().parameterChange().valueOf(SoundParameter.TEMPO));
        assertEquals(9, next.noteOn(1).orElseThrow().fret());
    }

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

    private static GuitarProFileWriter gp5BeatChangingTempo(int tempo, GuitarProVersion version) {
        boolean isGp510 = version == GuitarProVersion.GP5_10;
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
                .writeInt(tempo)
                .writeSignedByte(0);
        if (isGp510) {
            writer.writeBoolean(false);
        }
        writer.writeUnsignedByte(0x00).writeSignedByte(UNCHANGED);
        if (isGp510) {
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
