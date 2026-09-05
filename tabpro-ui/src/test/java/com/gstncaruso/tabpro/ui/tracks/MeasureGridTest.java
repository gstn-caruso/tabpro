package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MeasureGridTest {

    @Test
    void aCellSitsUnderItsMeasureAndBesideItsTrack() {
        MeasureGrid grid = new MeasureGrid(new Editor(Score.blank()));

        Rectangle first = grid.cellBounds(0, 0);
        Rectangle laterMeasure = grid.cellBounds(0, 3);
        Rectangle lowerTrack = grid.cellBounds(1, 0);

        assertEquals(0, first.x);
        assertEquals(TrackPanel.HEADER_HEIGHT, first.y);
        assertEquals(3 * MeasureGrid.CELL_WIDTH, laterMeasure.x);
        assertEquals(TrackPanel.HEADER_HEIGHT + TrackPanel.ROW_HEIGHT, lowerTrack.y);
    }

    @Test
    void findsTheCellUnderThePointer() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);
        Rectangle target = grid.cellBounds(1, 2);

        Optional<MeasureGrid.Cell> cell = grid.hitTest(target.x + 3, target.y + 3);

        assertEquals(Optional.of(new MeasureGrid.Cell(1, 2)), cell);
    }

    @Test
    void findsNothingOnTheHeaderOrPastTheLastMeasure() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);

        assertEquals(Optional.empty(), grid.hitTest(4, 3));
        assertEquals(Optional.empty(), grid.hitTest(40 * MeasureGrid.CELL_WIDTH, TrackPanel.HEADER_HEIGHT + 3));
        assertEquals(Optional.empty(), grid.hitTest(4, 10_000));
    }

    @Test
    void growsWithTheMeasuresAndTheTracks() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);

        assertEquals(3 * MeasureGrid.CELL_WIDTH, grid.getPreferredSize().width);
        assertEquals(TrackPanel.HEADER_HEIGHT + 2 * TrackPanel.ROW_HEIGHT, grid.getPreferredSize().height);
    }

    @Test
    void clickingACellSelectsThatTrackAndThatMeasure() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        editor.selectTrack(0);
        MeasureGrid grid = new MeasureGrid(editor);
        Rectangle target = grid.cellBounds(1, 2);

        grid.dispatchEvent(pressAt(grid, target.x + 3, target.y + 3));

        assertEquals(1, editor.cursor().track());
        assertEquals(2, editor.cursor().measure());
    }

    @Test
    void clickingOutsideTheGridChangesNothing() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);

        grid.dispatchEvent(pressAt(grid, 4, 3));

        assertEquals(0, editor.cursor().track());
        assertEquals(0, editor.cursor().measure());
    }

    @Test
    void aTrackThatPlaysNothingInAMeasureIsStillPartOfTheGrid() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);

        assertTrue(grid.hitTest(grid.cellBounds(1, 1).x + 2, grid.cellBounds(1, 1).y + 2).isPresent());
    }

    private static MouseEvent pressAt(MeasureGrid grid, int x, int y) {
        return new MouseEvent(grid, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, x, y, 1, false);
    }

    private static Editor editorWithTwoTracksAndThreeMeasures() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        editor.insertMeasure();
        editor.insertMeasure();
        editor.selectTrack(0);
        editor.moveTo(0, 0, 1);
        return editor;
    }
}
