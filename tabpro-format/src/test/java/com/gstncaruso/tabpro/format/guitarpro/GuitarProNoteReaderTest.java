package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuitarProNoteReaderTest {

    private static final int WITH_EFFECTS = 0x08;
    private static final int HAS_GRACE = 0x10;
    private static final int NO_MORE_EFFECTS = 0x00;

    private static final int FORTE = 6;
    private static final int THIRTY_SECOND = 1;
    private static final int SIXTEENTH = 3;
    private static final int HAMMER = 3;
    private static final int SLIDE = 1;

    private static final int HAS_SLIDE = 0x08;

    private static final int NO_FLAGS = 0x00;
    private static final int HAS_DYNAMIC = 0x10;
    private static final int PIANO = 3;

    private final GuitarProNoteReader reader = new GuitarProNoteReader();

    @Test
    void inGp4TheGraceNoteDurationComesBeforeTheTransition() {
        GraceNote grace = graceOf(read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(HAS_GRACE).writeUnsignedByte(NO_MORE_EFFECTS)
                .writeUnsignedByte(3).writeUnsignedByte(FORTE)
                .writeUnsignedByte(THIRTY_SECOND).writeUnsignedByte(HAMMER),
                GuitarProVersion.GP4));

        assertEquals(3, grace.fret());
        assertEquals(NoteValue.THIRTY_SECOND, grace.duration());
        assertEquals(GraceTransition.HAMMER, grace.transition());
    }

    @Test
    void inGp3TheDurationAlsoComesFirst() {
        GraceNote grace = graceOf(read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(HAS_GRACE)
                .writeUnsignedByte(5).writeUnsignedByte(FORTE)
                .writeUnsignedByte(SIXTEENTH).writeUnsignedByte(SLIDE),
                GuitarProVersion.GP3));

        assertEquals(NoteValue.SIXTEENTH, grace.duration());
        assertEquals(GraceTransition.SLIDE, grace.transition());
    }

    @Test
    void inGp5TheTransitionComesBeforeTheDuration() {
        GraceNote grace = graceOf(read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(0x00)
                .writeUnsignedByte(HAS_GRACE).writeUnsignedByte(NO_MORE_EFFECTS)
                .writeUnsignedByte(3).writeUnsignedByte(FORTE)
                .writeUnsignedByte(HAMMER).writeUnsignedByte(THIRTY_SECOND)
                .writeUnsignedByte(0x00),
                GuitarProVersion.GP5_00));

        assertEquals(NoteValue.THIRTY_SECOND, grace.duration());
        assertEquals(GraceTransition.HAMMER, grace.transition());
    }

    @Test
    void aNoteWithoutItsOwnDynamicIsForte() {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS),
                GuitarProVersion.GP4);

        assertEquals(Dynamic.FORTE, note.effects().dynamic());
    }

    @Test
    void aNoteThatBringsItsDynamicKeepsIt() {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_DYNAMIC)
                .writeSignedByte(PIANO),
                GuitarProVersion.GP4);

        assertEquals(Dynamic.PIANO, note.effects().dynamic());
    }

    @ParameterizedTest
    @CsvSource({"1, SHIFT", "2, LEGATO", "3, OUT_DOWNWARDS", "4, OUT_UPWARDS",
            "-1, IN_FROM_BELOW", "-2, IN_FROM_ABOVE"})
    void gp4WritesTheSlideAsASingleNumber(int written, SlideType expected) {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(NO_MORE_EFFECTS).writeUnsignedByte(HAS_SLIDE)
                .writeSignedByte(written),
                GuitarProVersion.GP4);

        assertEquals(expected, note.effects().slide().orElseThrow());
    }

    @ParameterizedTest
    @CsvSource({"0x01, SHIFT", "0x02, LEGATO", "0x04, OUT_DOWNWARDS", "0x08, OUT_UPWARDS",
            "0x10, IN_FROM_BELOW", "0x20, IN_FROM_ABOVE"})
    void gp5WritesTheSlideAsAMaskOfBits(int written, SlideType expected) {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(0x00)
                .writeUnsignedByte(NO_MORE_EFFECTS).writeUnsignedByte(HAS_SLIDE)
                .writeUnsignedByte(written),
                GuitarProVersion.GP5_10);

        assertEquals(expected, note.effects().slide().orElseThrow());
    }

    @Test
    void aGp5NoteWithSeveralSlidesKeepsTheFirstOne() {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(0x00)
                .writeUnsignedByte(NO_MORE_EFFECTS).writeUnsignedByte(HAS_SLIDE)
                .writeUnsignedByte(0x01 | 0x08),
                GuitarProVersion.GP5_10);

        assertEquals(SlideType.SHIFT, note.effects().slide().orElseThrow());
    }

    @Test
    void aGp5NoteWithoutSlidesInItsMaskHasNoSlide() {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(0x00)
                .writeUnsignedByte(NO_MORE_EFFECTS).writeUnsignedByte(HAS_SLIDE)
                .writeUnsignedByte(0x00),
                GuitarProVersion.GP5_10);

        assertTrue(note.effects().slide().isEmpty());
    }

    private static GraceNote graceOf(Note note) {
        return note.effects().grace().orElseThrow();
    }

    private Note read(GuitarProFileWriter written, GuitarProVersion version) {
        return reader.read(new GuitarProByteReader(written.bytes()), version, 1);
    }
}
