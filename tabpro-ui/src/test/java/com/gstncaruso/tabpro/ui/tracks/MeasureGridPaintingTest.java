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

/**
 * La grilla dice donde esta parada la edicion: el cuadrado del compas del cursor, en la fila de su
 * pista, va rodeado de un borde claro. Los tests miran el pixel del borde -no le preguntan a ningun
 * metodo si "esta marcado"-, porque lo que importa es que se vea.
 */
class MeasureGridPaintingTest {

    @Test
    void theCursorMeasureIsMarkedOnTheTrackTheCursorIsOn() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        editor.selectTrack(1);
        editor.moveTo(2, 0, 1);
        MeasureGrid grid = new MeasureGrid(editor);

        BufferedImage painted = paint(grid);

        assertEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 1, 2),
                "el compas donde esta el cursor tiene que quedar marcado");
        assertNotEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 1, 1),
                "los otros compases de la pista no");
        assertNotEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 0, 2),
                "ese compas en las otras pistas tampoco");
    }

    @Test
    void aMeasureWithoutNotesIsPaintedSilverLikeInGuitarPro5() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);

        BufferedImage painted = paint(grid);

        assertEquals(ScoreColors.EMPTY_MEASURE.getRGB(), centerOf(painted, grid, 0, 1),
                "un compas sin notas se pinta plata");
    }

    @Test
    void theMarkGoesWhereverTheCursorGoes() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);
        editor.selectTrack(0);
        editor.moveTo(0, 0, 1);
        assertEquals(ScoreColors.INK.getRGB(), topEdgeOf(paint(grid), grid, 0, 0), "el fixture arranca en 0");

        editor.selectTrack(1);
        editor.moveTo(2, 0, 1);

        BufferedImage painted = paint(grid);
        assertEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 1, 2));
        assertNotEquals(ScoreColors.INK.getRGB(), topEdgeOf(painted, grid, 0, 0),
                "la marca vieja no puede quedar pegada");
    }

    /** El pixel del medio del borde de arriba de esa celda. */
    private static int topEdgeOf(BufferedImage painted, MeasureGrid grid, int track, int measure) {
        Rectangle cell = grid.cellBounds(track, measure);
        return painted.getRGB(cell.x + cell.width / 2, cell.y + 1);
    }

    /** El pixel del medio de esa celda. */
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
