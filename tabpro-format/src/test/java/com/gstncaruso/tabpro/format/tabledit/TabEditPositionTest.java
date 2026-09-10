package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import java.util.List;
import org.junit.jupiter.api.Test;

class TabEditPositionTest {

    private static final int VALUE_PER_POSITION_PER_STRING = 32;
    private static final int GRID_POSITIONS_IN_FOUR_FOUR = 16;

    @Test
    void repartaMedidaYCuerdaDentroDeUnaSolaPista() {
        List<TabEditMeasure> measures = List.of(measure(4, 4), measure(4, 4));
        List<Integer> trackStringCounts = List.of(6);
        int valuePerPosition = VALUE_PER_POSITION_PER_STRING * 6;

        int location = 5 * valuePerPosition + 2 * 8;

        TabEditPosition position = TabEditPosition.fromLocation(location, measures, trackStringCounts);

        assertEquals(0, position.measureIndex());
        assertEquals(5, position.positionInMeasure());
        assertEquals(2, position.stringZeroBased());
        assertEquals(0, position.trackIndex());
    }

    @Test
    void avanzaAlSegundoCompasSegunSuPropiaMedida() {
        List<TabEditMeasure> measures = List.of(measure(4, 4), measure(3, 4));
        List<Integer> trackStringCounts = List.of(6);
        int valuePerPosition = VALUE_PER_POSITION_PER_STRING * 6;
        int wholeFirstMeasure = valuePerPosition * GRID_POSITIONS_IN_FOUR_FOUR;

        int location = wholeFirstMeasure + 2 * valuePerPosition + 0;

        TabEditPosition position = TabEditPosition.fromLocation(location, measures, trackStringCounts);

        assertEquals(1, position.measureIndex());
        assertEquals(2, position.positionInMeasure());
        assertEquals(0, position.stringZeroBased());
    }

    @Test
    void reconoceLaPistaSegunLaCuerdaAcumulada() {
        List<TabEditMeasure> measures = List.of(measure(4, 4));
        List<Integer> trackStringCounts = List.of(4, 6);
        int totalStrings = 10;
        int valuePerPosition = VALUE_PER_POSITION_PER_STRING * totalStrings;

        int location = 1 * valuePerPosition + 4 * 8;

        TabEditPosition position = TabEditPosition.fromLocation(location, measures, trackStringCounts);

        assertEquals(1, position.trackIndex());
        assertEquals(0, position.stringZeroBased());
    }

    private static TabEditMeasure measure(int numerator, int denominator) {
        return new TabEditMeasure(new TimeSignature(numerator, denominator), KeySignature.cMajor());
    }
}
