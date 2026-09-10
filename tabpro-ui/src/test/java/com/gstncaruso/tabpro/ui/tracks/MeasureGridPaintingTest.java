package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class MeasureGridPaintingTest {

    @Test
    void theCursorMeasureIsMarkedOnTheTrackTheCursorIsOn() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        editor.selectTrack(1);
        editor.moveTo(2, 0, 1);
        MeasureGrid grid = new MeasureGrid(editor);

        BufferedImage painted = paint(grid);

        assertEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 1, 2),
                "the measure where the cursor is has to be marked");
        assertNotEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 1, 1),
                "the other measures of the track are not");
        assertNotEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 0, 2),
                "nor is that measure on the other tracks");
    }

    @Test
    void aMeasureWithoutNotesIsPaintedSilverLikeInGuitarPro5() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);

        BufferedImage painted = paint(grid);

        assertEquals(ScoreColors.EMPTY_MEASURE.getRGB(), centerOf(painted, grid, 0, 1),
                "a measure without notes is painted silver");
    }

    @Test
    void theMarkGoesWhereverTheCursorGoes() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);
        editor.selectTrack(0);
        editor.moveTo(0, 0, 1);
        assertEquals(ScoreColors.INK.getRGB(), topEdgeOf(paint(grid), grid, 0, 0), "the fixture starts at 0");

        editor.selectTrack(1);
        editor.moveTo(2, 0, 1);

        BufferedImage painted = paint(grid);
        assertEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 1, 2));
        assertNotEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 0, 0),
                "the old mark cannot stay stuck");
    }

    private static int topEdgeOf(BufferedImage painted, MeasureGrid grid, int track, int measure) {
        Rectangle cell = grid.cellBounds(track, measure);
        return painted.getRGB(cell.x + cell.width / 2, cell.y + 1);
    }

    private static int centerOf(BufferedImage painted, MeasureGrid grid, int track, int measure) {
        Rectangle cell = grid.cellBounds(track, measure);
        return painted.getRGB(cell.x + cell.width / 2, cell.y + cell.height / 2);
    }

    private static BufferedImage paint(MeasureGrid grid) {
        Dimension size = grid.getPreferredSize();
        grid.setSize(size);
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        grid.paint(g);
        g.dispose();
        return image;
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
