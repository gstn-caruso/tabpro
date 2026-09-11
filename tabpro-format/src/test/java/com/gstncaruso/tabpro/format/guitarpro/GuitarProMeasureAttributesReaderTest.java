package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import com.gstncaruso.tabpro.format.TestDefaultNames;
import java.util.List;
import org.junit.jupiter.api.Test;

class GuitarProMeasureAttributesReaderTest {

    private static final int HAS_ALTERNATE_ENDINGS = 0x10;
    private static final int OPENS_REPEAT = 0x04;

    private final GuitarProMeasureAttributesReader reader = new GuitarProMeasureAttributesReader(
            TimeSignature.fourFour(), KeySignature.cMajor(), TripletFeel.NONE, new TestDefaultNames());

    @Test
    void beforeGp5TheByteSaysHowFarTheEndingGoes() {
        assertEquals(List.of(1, 2), endingsOf(2, GuitarProVersion.GP4));
    }

    @Test
    void everyMeasureTakesTheRoundsTheOnesBeforeItLeft() {
        assertEquals(List.of(1, 2, 3), endingsOf(3, GuitarProVersion.GP4));
        assertEquals(List.of(4), endingsOf(4, GuitarProVersion.GP4));
        assertEquals(List.of(5, 6, 7, 8), endingsOf(8, GuitarProVersion.GP4));
    }

    @Test
    void aNewRepeatStartsCountingTheRoundsAgain() {
        assertEquals(List.of(1, 2), endingsOf(2, GuitarProVersion.GP4));
        assertEquals(List.of(), opensRepeatWith(0, GuitarProVersion.GP4));
        assertEquals(List.of(1, 2), endingsOf(2, GuitarProVersion.GP4));
    }

    @Test
    void gp3CountsTheRoundsTheSameWay() {
        assertEquals(List.of(1, 2, 3), endingsOf(3, GuitarProVersion.GP3));
    }

    @Test
    void gp5WritesTheRoundsAsAMaskOfBits() {
        assertEquals(List.of(1, 3), gp5EndingsOf(0x05));
    }

    private List<Integer> endingsOf(int written, GuitarProVersion version) {
        return read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_ALTERNATE_ENDINGS)
                .writeUnsignedByte(written), version);
    }

    private List<Integer> opensRepeatWith(int written, GuitarProVersion version) {
        return read(new GuitarProFileWriter().writeUnsignedByte(OPENS_REPEAT), version);
    }

    private List<Integer> gp5EndingsOf(int mask) {
        return read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_ALTERNATE_ENDINGS)
                .writeUnsignedByte(mask)
                .writeUnsignedByte(0), GuitarProVersion.GP5_10);
    }

    private List<Integer> read(GuitarProFileWriter written, GuitarProVersion version) {
        return reader.read(new GuitarProByteReader(written.bytes()), version, true)
                .attributes()
                .alternateEndings();
    }
}
