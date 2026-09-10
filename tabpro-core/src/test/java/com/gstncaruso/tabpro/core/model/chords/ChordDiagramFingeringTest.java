package com.gstncaruso.tabpro.core.model.chords;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChordDiagramFingeringTest {

    private static final ChordDiagram OPEN_C = ChordDiagram.named("C", List.of(0, 1, 0, 2, 3, -1));

    private static final ChordDiagram BARRE_F = ChordDiagram.named("F", List.of(1, 1, 2, 3, 3, 1));

    @Test
    void anOpenChordDoesNotNeedABarre() {
        assertFalse(OPEN_C.requiresBarre());
        assertTrue(OPEN_C.barreFret().isEmpty());
    }

    @Test
    void moreThanFourFrettedStringsNeedABarre() {
        assertTrue(BARRE_F.requiresBarre());
        assertEquals(1, BARRE_F.barreFret().orElseThrow());
    }

    @Test
    void theSpanIsTheDistanceBetweenTheLowestAndHighestFret() {
        assertEquals(2, OPEN_C.fretSpan());
        assertEquals(2, BARRE_F.fretSpan());
    }

    @Test
    void autoFingersTheOpenCMajorChord() {
        ChordDiagram fingered = OPEN_C.autoFingered();
        assertEquals(Finger.INDEX, fingered.fingerOfString(2).orElseThrow());
        assertEquals(Finger.MIDDLE, fingered.fingerOfString(4).orElseThrow());
        assertEquals(Finger.RING, fingered.fingerOfString(5).orElseThrow());
        assertTrue(fingered.fingerOfString(1).isEmpty());
        assertTrue(fingered.fingerOfString(3).isEmpty());
    }

    @Test
    void autoFingersTheFBarreChord() {
        ChordDiagram fingered = BARRE_F.autoFingered();
        assertEquals(Finger.INDEX, fingered.fingerOfString(1).orElseThrow());
        assertEquals(Finger.INDEX, fingered.fingerOfString(2).orElseThrow());
        assertEquals(Finger.INDEX, fingered.fingerOfString(6).orElseThrow());
        assertEquals(Finger.MIDDLE, fingered.fingerOfString(3).orElseThrow());
        assertEquals(Finger.RING, fingered.fingerOfString(4).orElseThrow());
        assertEquals(Finger.LITTLE, fingered.fingerOfString(5).orElseThrow());
    }

    @Test
    void aSimpleOpenChordIsSimple() {
        assertEquals(ChordComplexity.SIMPLE, OPEN_C.complexity());
    }

    @Test
    void aFirstPositionBarreIsMedium() {
        assertEquals(ChordComplexity.MEDIUM, BARRE_F.complexity());
    }

    @Test
    void aBarreFarFromTheNutWithALotOfSpanIsComplex() {
        ChordDiagram farChord = ChordDiagram.named("X", List.of(10, 12, 11, 13, 13, 10));
        assertEquals(ChordComplexity.COMPLEX, farChord.complexity());
    }

    @Test
    void theShapeOfAnOpenChordIsItsOwnFrets() {
        assertEquals(List.of(0, 1, 0, 2, 3, -1), OPEN_C.shape());
    }

    @Test
    void aBarreKeepsItsShapeWhenMovingFrets() {
        ChordDiagram gBarreChord = new ChordDiagram("G", 3, List.of(3, 3, 4, 5, 5, 3), List.of(), true);

        assertEquals(BARRE_F.shape(), gBarreChord.shape());
    }

    @Test
    void openAndMutedStringsDoNotShiftWithTheBaseFret() {
        ChordDiagram atFifthFret = new ChordDiagram("X", 5, List.of(0, 5, 7, 7, 5, -1), List.of(), true);

        assertEquals(List.of(0, 1, 3, 3, 1, -1), atFifthFret.shape());
    }
}
