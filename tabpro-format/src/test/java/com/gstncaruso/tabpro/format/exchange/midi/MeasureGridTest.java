package com.gstncaruso.tabpro.format.exchange.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class MeasureGridTest {

    @Test
    void fillsAWholeFileWithASingleTimeSignature() {
        TreeMap<Long, TimeSignature> signatures = new TreeMap<>();
        signatures.put(0L, TimeSignature.fourFour());
        TreeMap<Long, KeySignature> keys = new TreeMap<>();
        keys.put(0L, KeySignature.cMajor());
        long ticksPerMeasure = TimeSignature.fourFour().ticksPerMeasure();

        MeasureGrid grid = MeasureGrid.build(signatures, keys, ticksPerMeasure * 3);

        assertEquals(3, grid.measureCount());
        assertEquals(0L, grid.startTick(0));
        assertEquals(ticksPerMeasure, grid.startTick(1));
        assertEquals(ticksPerMeasure * 2, grid.startTick(2));
        assertEquals(TimeSignature.fourFour(), grid.timeSignatureOf(2));
    }

    @Test
    void switchesTimeSignatureAtTheDeclaredMeasure() {
        long ticksPerMeasureFourFour = TimeSignature.fourFour().ticksPerMeasure();
        TimeSignature threeFour = new TimeSignature(3, 4);
        TreeMap<Long, TimeSignature> signatures = new TreeMap<>();
        signatures.put(0L, TimeSignature.fourFour());
        signatures.put(ticksPerMeasureFourFour, threeFour);
        TreeMap<Long, KeySignature> keys = new TreeMap<>();
        keys.put(0L, KeySignature.cMajor());

        MeasureGrid grid = MeasureGrid.build(signatures, keys, ticksPerMeasureFourFour + threeFour.ticksPerMeasure() * 2);

        assertEquals(3, grid.measureCount());
        assertEquals(TimeSignature.fourFour(), grid.timeSignatureOf(0));
        assertEquals(threeFour, grid.timeSignatureOf(1));
        assertEquals(threeFour, grid.timeSignatureOf(2));
    }

    @Test
    void alwaysHasAtLeastOneMeasureEvenForAnEmptyFile() {
        TreeMap<Long, TimeSignature> signatures = new TreeMap<>();
        signatures.put(0L, TimeSignature.fourFour());
        TreeMap<Long, KeySignature> keys = new TreeMap<>();
        keys.put(0L, KeySignature.cMajor());

        MeasureGrid grid = MeasureGrid.build(signatures, keys, 0);

        assertEquals(1, grid.measureCount());
    }
}
