package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import org.junit.jupiter.api.Test;

class GuitarProBeatWriterTest {

    private static final int HAS_STATUS = 0x40;
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_REST = 2;
    private static final int QUARTER = 0;
    private static final int NO_STRINGS = 0x00;
    private static final int ONLY_FIRST_STRING = 0x40;

    private static final int UNSET = -1;

    private static final int MIX_TABLE_AT = 3;

    private final GuitarProBeatWriter writer = new GuitarProBeatWriter();

    @Test
    void aNormalNoteIsNotAnEmptyBeat() {
        byte[] bytes = write(Beat.of(Duration.quarter(), new Note(1, 5)));

        assertEquals(HAS_STATUS, bytes[0] & 0xFF);
        assertEquals(STATUS_NORMAL, bytes[1] & 0xFF);
        assertEquals(QUARTER, bytes[2]);
        assertEquals(ONLY_FIRST_STRING, bytes[3] & 0xFF);
    }

    @Test
    void aRestAlsoWritesItsStringMask() {
        byte[] bytes = write(Beat.rest(Duration.quarter()));

        assertEquals(STATUS_REST, bytes[1] & 0xFF);
        assertEquals(QUARTER, bytes[2]);
        assertEquals(NO_STRINGS, bytes[3] & 0xFF);
        assertEquals(4, bytes.length, "a rest is exactly four bytes");
    }

    @Test
    void theParameterChangeWritesTheKnobsInTheirSteps() {
        ParameterChange change = ParameterChange.nothing()
                .changing(SoundParameter.VOLUME, 104)
                .changing(SoundParameter.PAN, 64);
        byte[] bytes = write(Beat.rest(Duration.quarter())
                .withEffects(BeatEffects.none().withParameterChange(change)));

        assertEquals(UNSET, bytes[MIX_TABLE_AT], "the instrument does not change");
        assertEquals(13, bytes[MIX_TABLE_AT + 1]);
        assertEquals(8, bytes[MIX_TABLE_AT + 2]);
    }

    private byte[] write(Beat beat) {
        GuitarProByteWriter bytes = new GuitarProByteWriter();
        writer.write(bytes, beat);
        return bytes.bytes();
    }
}
