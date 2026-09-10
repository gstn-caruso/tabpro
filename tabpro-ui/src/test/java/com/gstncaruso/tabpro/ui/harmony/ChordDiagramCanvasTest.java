package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class ChordDiagramCanvasTest {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 260;

    @Test
    void stringSixStaysOnTheLeftAndStringOneOnTheRight() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        assertTrue(canvas.stringX(6) < canvas.stringX(1), "the lowest string goes on the left, as in the manual");
        assertTrue(canvas.stringX(6) < canvas.stringX(5));
    }

    @Test
    void theStringsAreEvenlySpaced() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        assertEquals(
                canvas.stringX(2) - canvas.stringX(1),
                canvas.stringX(6) - canvas.stringX(5));
    }

    @Test
    void eachFretRowStaysBelowThePrevious() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        assertTrue(canvas.fretRowY(0) < canvas.fretRowY(1));
        assertTrue(canvas.fretRowY(1) < canvas.fretRowY(2));
    }

    @Test
    void identifiesTheClickedStringByItsXPosition() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        for (int string = 1; string <= 6; string++) {
            assertEquals(OptionalInt.of(string), canvas.stringAt(canvas.stringX(string)));
        }
    }

    @Test
    void noStringIsFoundOutsideTheDiagram() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        assertEquals(OptionalInt.empty(), canvas.stringAt(-100));
        assertEquals(OptionalInt.empty(), canvas.stringAt(WIDTH + 100));
    }

    @Test
    void aClickOnTheGridGivesTheAbsoluteFretAccordingToTheBaseFret() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        canvas.show(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1)), Tuning.standard());

        assertEquals(OptionalInt.of(1), canvas.fretAt(canvas.fretRowY(0)));
        assertEquals(OptionalInt.of(2), canvas.fretAt(canvas.fretRowY(1)));
    }

    @Test
    void aDiagramInAHighPositionStartsTheGridAtItsBaseFret() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        ChordDiagram atTheFifthFret = ChordDiagrams.withBaseFret(
                ChordDiagram.named("Am (cejilla)", List.of(5, 5, 5, 7, 7, 5)), 5);
        canvas.show(atTheFifthFret, Tuning.standard());

        assertEquals(OptionalInt.of(5), canvas.fretAt(canvas.fretRowY(0)));
        assertEquals(OptionalInt.of(6), canvas.fretAt(canvas.fretRowY(1)));
    }

    @Test
    void theHeaderOnlyAppearsWhenTheBaseFretIsOne() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        canvas.show(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1)), Tuning.standard());
        assertTrue(canvas.hasHeader());

        canvas.show(ChordDiagrams.withBaseFret(ChordDiagram.named("X", List.of(5, 5, 5, 7, 7, 5)), 5), Tuning.standard());
        assertFalse(canvas.hasHeader());
    }

    @Test
    void theFingeringRowStaysBelowTheLastFretRow() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        canvas.show(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1)), Tuning.standard());

        assertTrue(canvas.fingerRowY() > canvas.fretRowY(canvas.rowCount() - 1));
        assertTrue(canvas.isFingerRow(canvas.fingerRowY()));
        assertFalse(canvas.isFingerRow(canvas.fretRowY(0)));
    }

    @Test
    void aClickOnTheFingeringRowReportsTheString() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        canvas.show(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1)), Tuning.standard());
        java.util.concurrent.atomic.AtomicInteger reportedString = new java.util.concurrent.atomic.AtomicInteger(-1);
        canvas.onFingerClick(reportedString::set);

        canvas.dispatchEvent(new java.awt.event.MouseEvent(
                canvas, java.awt.event.MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                canvas.stringX(2), canvas.fingerRowY(), 1, false));

        assertEquals(2, reportedString.get());
    }

    @Test
    void identifiesTheHeaderOfEachString() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        canvas.show(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1)), Tuning.standard());

        assertEquals(OptionalInt.of(6), canvas.stringAt(canvas.stringX(6)));
        assertTrue(canvas.isHeaderRow(canvas.headerY()));
        assertFalse(canvas.isHeaderRow(canvas.fretRowY(2)));
    }

    private static ChordDiagramCanvas sized(ChordDiagramCanvas canvas) {
        canvas.setSize(WIDTH, HEIGHT);
        return canvas;
    }
}
