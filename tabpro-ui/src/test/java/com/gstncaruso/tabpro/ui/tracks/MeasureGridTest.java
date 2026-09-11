package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class MeasureGridTest {

    @Test
    void theAccessibleNameIsAvailableInEnglish() {
        assertEquals("Bar Grid", Texts.forLocale(Locale.ENGLISH).text("views.MeasureGrid.name"));
    }

    @Test
    void aCellSitsUnderItsMeasureAndBesideItsTrack() {
        MeasureGrid grid = new MeasureGrid(new Editor(Score.blank(new TextsDefaultNames())));

        Rectangle first = grid.cellBounds(0, 0);
        Rectangle laterMeasure = grid.cellBounds(0, 3);
        Rectangle lowerTrack = grid.cellBounds(1, 0);

        assertEquals(0, first.x);
        assertEquals(MeasureGrid.NUMBERS_HEIGHT, first.y);
        assertEquals(3 * MeasureGrid.CELL_WIDTH, laterMeasure.x);
        assertEquals(MeasureGrid.NUMBERS_HEIGHT + TrackPanel.ROW_HEIGHT, lowerTrack.y);
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
        assertEquals(Optional.empty(), grid.hitTest(40 * MeasureGrid.CELL_WIDTH, MeasureGrid.NUMBERS_HEIGHT + 3));
        assertEquals(Optional.empty(), grid.hitTest(4, 10_000));
    }

    @Test
    void growsWithTheMeasuresAndTheTracks() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);

        assertEquals(3 * MeasureGrid.CELL_WIDTH, grid.getPreferredSize().width);
        assertEquals(MeasureGrid.NUMBERS_HEIGHT + 2 * TrackPanel.ROW_HEIGHT, grid.getPreferredSize().height);
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

    @Test
    void theRightArrowKeyMovesTheCaretToTheNextMeasure() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);

        pressShortcut(grid, KeyStroke.getKeyStroke("RIGHT"));

        assertEquals(new MeasureGrid.Cell(0, 1), grid.caret());
    }

    @Test
    void theLeftArrowKeyMovesTheCaretToThePreviousMeasure() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        editor.moveTo(2, 0, 1);
        MeasureGrid grid = new MeasureGrid(editor);

        pressShortcut(grid, KeyStroke.getKeyStroke("LEFT"));

        assertEquals(new MeasureGrid.Cell(0, 1), grid.caret());
    }

    @Test
    void theDownArrowKeyMovesTheCaretToTheNextTrack() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);

        pressShortcut(grid, KeyStroke.getKeyStroke("DOWN"));

        assertEquals(new MeasureGrid.Cell(1, 0), grid.caret());
    }

    @Test
    void theUpArrowKeyMovesTheCaretToThePreviousTrack() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        editor.selectTrack(1);
        MeasureGrid grid = new MeasureGrid(editor);

        pressShortcut(grid, KeyStroke.getKeyStroke("UP"));

        assertEquals(new MeasureGrid.Cell(0, 0), grid.caret());
    }

    @Test
    void theEnterKeyMovesTheCursorToTheCaretLikeAClickWould() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);
        pressShortcut(grid, KeyStroke.getKeyStroke("RIGHT"));
        pressShortcut(grid, KeyStroke.getKeyStroke("DOWN"));

        pressShortcut(grid, KeyStroke.getKeyStroke("ENTER"));

        assertEquals(1, editor.cursor().track());
        assertEquals(1, editor.cursor().measure());
    }

    @Test
    void paintsAVisibleCaretRingWhenItGetsFocus() {
        Editor editor = editorWithTwoTracksAndThreeMeasures();
        MeasureGrid grid = new MeasureGrid(editor);
        grid.setSize(grid.getPreferredSize());
        BufferedImage withoutFocus = paint(grid);

        gainFocus(grid);
        BufferedImage withFocus = paint(grid);

        assertTrue(differsSomewhere(withoutFocus, withFocus), "the focus has to be visible in the drawing");
    }

    private static void gainFocus(MeasureGrid grid) {
        for (var listener : grid.getFocusListeners()) {
            listener.focusGained(new FocusEvent(grid, FocusEvent.FOCUS_GAINED));
        }
    }

    private static BufferedImage paint(MeasureGrid grid) {
        BufferedImage image = new BufferedImage(grid.getWidth(), grid.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        grid.paint(g);
        g.dispose();
        return image;
    }

    private static boolean differsSomewhere(BufferedImage a, BufferedImage b) {
        for (int x = 0; x < a.getWidth(); x++) {
            for (int y = 0; y < a.getHeight(); y++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void pressShortcut(JComponent component, KeyStroke keyStroke) {
        Object name = component.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name).actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
    }

    private static MouseEvent pressAt(MeasureGrid grid, int x, int y) {
        return new MouseEvent(grid, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, x, y, 1, false);
    }

    private static Editor editorWithTwoTracksAndThreeMeasures() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.addTrack(Track.standardBass("Bajo"));
        editor.insertMeasure();
        editor.insertMeasure();
        editor.selectTrack(0);
        editor.moveTo(0, 0, 1);
        return editor;
    }

    @Test
    void hasAnAccessibleNameAndTooltip() {
        MeasureGrid grid = new MeasureGrid(new Editor(Score.blank(new TextsDefaultNames())));

        assertEquals("Grilla de compases", grid.getAccessibleContext().getAccessibleName());
        assertTrue(grid.getToolTipText() != null && !grid.getToolTipText().isBlank());
    }
}
