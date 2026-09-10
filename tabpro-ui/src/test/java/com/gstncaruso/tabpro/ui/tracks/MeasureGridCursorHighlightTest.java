package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Auditoria de rendimiento, hallazgo 4: TrackPanel.editorChanged() llamaba a
 * GlobalView.refresh(), que revalida y repinta toda la grilla de compases -O(compases x pistas)-
 * con cada flecha, aunque solo se movio el cursor.
 */
class MeasureGridCursorHighlightTest {

    @Test
    void movingTheCursorHighlightSkipsRevalidateAndRepaintsOnlyTheTwoCells() {
        Editor editor = editorWithTwoMeasures();
        SpyingMeasureGrid spy = new SpyingMeasureGrid(editor);
        spy.forgetCallsMadeWhileBuilding();
        Rectangle before = spy.cellBounds(0, 0);

        editor.moveToNextMeasure();
        spy.moveCursorHighlight();

        Rectangle after = spy.cellBounds(0, 1);
        assertEquals(0, spy.revalidateCalls);
        assertFalse(spy.fullRepaintCalled);
        assertEquals(List.of(before.union(after)), spy.repaintedAreas);
    }

    private static Editor editorWithTwoMeasures() {
        List<Measure> measures = new ArrayList<>();
        measures.add(Measure.empty(new TimeSignature(1, 4), Duration.quarter()));
        measures.add(Measure.empty(new TimeSignature(1, 4), Duration.quarter()));
        Track guitar = Track.standardGuitar("Guitarra").withMeasures(measures);
        return new Editor(new Score("Prueba", 120, List.of(guitar)));
    }

    private static final class SpyingMeasureGrid extends MeasureGrid {
        private int revalidateCalls;
        private boolean fullRepaintCalled;
        private final List<Rectangle> repaintedAreas = new ArrayList<>();

        SpyingMeasureGrid(Editor editor) {
            super(editor);
        }

        @Override
        public void revalidate() {
            revalidateCalls++;
            super.revalidate();
        }

        @Override
        public void repaint() {
            fullRepaintCalled = true;
            super.repaint();
        }

        @Override
        public void repaint(Rectangle area) {
            repaintedAreas.add(area);
            super.repaint(area);
        }

        void forgetCallsMadeWhileBuilding() {
            revalidateCalls = 0;
            fullRepaintCalled = false;
            repaintedAreas.clear();
        }
    }
}
