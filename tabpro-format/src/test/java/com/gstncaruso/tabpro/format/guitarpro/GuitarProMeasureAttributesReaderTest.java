package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Los finales alternativos no se guardan igual antes y despues de GP5. Los numeros de
 * estos casos son los bytes que trae "Repeat.gp4", de la suite de PyGuitarPro: 3, 4 y 8
 * en tres compases seguidos, que son los finales 1-2-3, el 4 y el 5-6-7-8.
 */
class GuitarProMeasureAttributesReaderTest {

    private static final int HAS_ALTERNATE_ENDINGS = 0x10;
    private static final int OPENS_REPEAT = 0x04;

    private final GuitarProMeasureAttributesReader reader = new GuitarProMeasureAttributesReader(
            TimeSignature.fourFour(), KeySignature.cMajor(), TripletFeel.NONE);

    /**
     * Hasta GP4 el byte es hasta que vuelta llega el compas, no que vueltas toca: un 2 es
     * "las vueltas 1 y 2", no "la vuelta 2". Leerlo como mascara pierde vueltas enteras.
     */
    @Test
    void beforeGp5TheByteSaysHowFarTheEndingGoes() {
        assertEquals(List.of(1, 2), endingsOf(2, GuitarProVersion.GP4));
    }

    /** Cada compas se lleva las vueltas que los anteriores de la misma repeticion no usaron. */
    @Test
    void everyMeasureTakesTheRoundsTheOnesBeforeItLeft() {
        assertEquals(List.of(1, 2, 3), endingsOf(3, GuitarProVersion.GP4));
        assertEquals(List.of(4), endingsOf(4, GuitarProVersion.GP4));
        assertEquals(List.of(5, 6, 7, 8), endingsOf(8, GuitarProVersion.GP4));
    }

    /** Una repeticion nueva vuelve a empezar la cuenta de vueltas ya usadas. */
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

    /** En GP5 el mismo byte pasa a ser una mascara: cada bit es una vuelta. */
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

    /** En GP5 el final alternativo se mudo al final del master bar, detras del relleno. */
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
