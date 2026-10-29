package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.OptionalInt;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class GlobalViewTest {

    @Test
    void stacksTheMarkerZoneOnTopOfTheGrid() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        GlobalView view = new GlobalView(editor);

        Dimension size = view.getPreferredSize();

        assertEquals(MeasureGrid.CELL_WIDTH, size.width);
        assertEquals(MarkerZone.HEIGHT + view.grid().getPreferredSize().height, size.height);
    }

    @Test
    void givenPlayingMeasureManuallyScrolledOut_whenSameMeasureNotified_thenMeasureVisible() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
            for (int measure = 1; measure < 30; measure++) {
                editor.insertMeasure();
            }
            GlobalView view = new GlobalView(editor);
            MeasureGrid grid = view.grid();
            JScrollPane pane = new JScrollPane(view);
            view.setSize(view.getPreferredSize());
            pane.setSize(8 * grid.cellBounds(0, 0).width, view.getPreferredSize().height + 50);
            pane.doLayout();
            pane.getViewport().doLayout();
            view.doLayout();
            var cursorBefore = editor.cursor();
            var caretBefore = grid.caret();
            int playingMeasure = 16;
            Rectangle playingCell = SwingUtilities.convertRectangle(
                    grid, grid.cellBounds(0, playingMeasure), view);
            var viewport = pane.getViewport();
            viewport.setViewPosition(new Point(playingCell.x, 0));
            Rectangle initiallyVisible = viewport.getViewRect();
            assertTrue(initiallyVisible.x <= playingCell.x
                            && initiallyVisible.x + initiallyVisible.width >= playingCell.x + playingCell.width,
                    "setup must expose the entire playing measure through manual scrolling");
            view.showPlayingMeasure(OptionalInt.of(playingMeasure));
            viewport.setViewPosition(new Point(0, 0));
            assertTrue(viewport.getViewRect().x + viewport.getViewRect().width <= playingCell.x,
                    "manual scrolling must hide the playing measure before the repeated notification");

            view.showPlayingMeasure(OptionalInt.of(playingMeasure));

            Rectangle visible = viewport.getViewRect();
            assertAll(
                    () -> assertTrue(visible.x <= playingCell.x
                                    && visible.x + visible.width >= playingCell.x + playingCell.width,
                            "the repeated playing-measure notification must restore full horizontal visibility: "
                                    + "viewport=" + visible + ", playing cell=" + playingCell),
                    () -> assertEquals(cursorBefore, editor.cursor(), "playback following must not move the edit cursor"),
                    () -> assertEquals(caretBefore, grid.caret(), "playback following must not move the grid caret"));
        });
    }

    @Test
    void givenVerticallyScrolledManyTrackGrid_whenPlaybackReachesEndAndRepeats_thenMeasureFullyVisibleWithoutVerticalScroll()
            throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
            for (int measure = 1; measure < 30; measure++) {
                editor.insertMeasure();
            }
            for (int track = 1; track < 6; track++) {
                editor.addTrack(Track.standardBass("Bass " + track));
            }
            GlobalView view = new GlobalView(editor);
            MeasureGrid grid = view.grid();
            JScrollPane pane = new JScrollPane(view);
            view.setSize(view.getPreferredSize());
            pane.setSize(8 * grid.cellBounds(0, 0).width, 3 * grid.cellBounds(0, 0).height);
            pane.doLayout();
            pane.getViewport().doLayout();
            view.doLayout();
            var viewport = pane.getViewport();
            Rectangle firstCell = SwingUtilities.convertRectangle(grid, grid.cellBounds(0, 0), view);
            int lastMeasure = editor.score().measureCount() - 1;
            Rectangle lastCell = SwingUtilities.convertRectangle(grid, grid.cellBounds(0, lastMeasure), view);
            int verticalPosition = SwingUtilities.convertPoint(grid, 0, grid.cellBounds(3, 0).y, view).y;
            viewport.setViewPosition(new Point(0, verticalPosition));
            assertAll("setup must require horizontal following below the marker zone",
                    () -> assertTrue(grid.getY() > 0, "the marker zone must offset the grid"),
                    () -> assertTrue(viewport.getViewRect().width >= firstCell.width,
                            "the viewport must fit a complete measure"),
                    () -> assertTrue(viewport.getViewRect().width < lastCell.x + lastCell.width - firstCell.x,
                            "the first and last measures must not fit simultaneously"),
                    () -> assertEquals(verticalPosition, viewport.getViewPosition().y),
                    () -> assertTrue(verticalPosition > grid.getY(),
                            "manual vertical scrolling must extend beyond the marker zone offset"));
            var cursorBefore = editor.cursor();
            var caretBefore = grid.caret();

            // Play every measure, then repeat from the first one.
            for (int step = 0; step <= editor.score().measureCount(); step++) {
                int measure = step % editor.score().measureCount();
                view.showPlayingMeasure(OptionalInt.of(measure));

                Rectangle cell = SwingUtilities.convertRectangle(grid, grid.cellBounds(0, measure), view);
                Rectangle visible = viewport.getViewRect();
                assertAll("playing measure " + measure + " at step " + step,
                        () -> assertTrue(visible.x <= cell.x
                                        && visible.x + visible.width >= cell.x + cell.width,
                                "the entire audible measure must remain horizontally visible: viewport="
                                        + visible + ", cell=" + cell),
                        () -> assertEquals(verticalPosition, visible.y,
                                "following playback must preserve manual vertical scrolling"),
                        () -> assertEquals(cursorBefore, editor.cursor(), "playback must not move the edit cursor"),
                        () -> assertEquals(caretBefore, grid.caret(), "playback must not move the grid caret"));
            }
        });
    }

    @Test
    void givenGridScrolledDuringPlayback_whenPlaybackCleared_thenViewportUnchanged() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
            for (int measure = 1; measure < 16; measure++) {
                editor.insertMeasure();
            }
            for (int track = 1; track < 4; track++) {
                editor.addTrack(Track.standardBass("Bass " + track));
            }
            GlobalView view = new GlobalView(editor);
            MeasureGrid grid = view.grid();
            JScrollPane pane = new JScrollPane(view);
            view.setSize(view.getPreferredSize());
            pane.setSize(6 * grid.cellBounds(0, 0).width, 2 * grid.cellBounds(0, 0).height);
            pane.doLayout();
            pane.getViewport().doLayout();
            view.doLayout();
            int playingMeasure = editor.score().measureCount() - 1;
            view.showPlayingMeasure(OptionalInt.of(playingMeasure));
            var viewport = pane.getViewport();
            int verticalPosition = SwingUtilities.convertPoint(grid, 0, grid.cellBounds(2, 0).y, view).y;
            viewport.setViewPosition(new Point(grid.cellBounds(0, 4).x, verticalPosition));
            Point positionBefore = viewport.getViewPosition();
            Rectangle playingCell = SwingUtilities.convertRectangle(
                    grid, grid.cellBounds(0, playingMeasure), view);
            assertAll("setup must manually scroll both axes and hide the playing measure",
                    () -> assertTrue(positionBefore.x > 0, "horizontal scrolling must be nonzero"),
                    () -> assertTrue(grid.getY() > 0, "the marker zone must offset the grid"),
                    () -> assertTrue(positionBefore.y > grid.getY(),
                            "vertical scrolling must extend beyond the marker zone offset"),
                    () -> assertTrue(viewport.getViewRect().width >= playingCell.width,
                            "the viewport must fit a complete measure"),
                    () -> assertTrue(viewport.getViewRect().x + viewport.getViewRect().width <= playingCell.x,
                            "manual scrolling must hide the playing measure"));

            view.showPlayingMeasure(OptionalInt.empty());

            assertEquals(positionBefore, viewport.getViewPosition(),
                    "clearing playback must preserve both horizontal and vertical manual scrolling");
        });
    }

    @Test
    void movingTheCursorHighlightDelegatesToTheGridWithoutRevalidating() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        SpyingMeasureGrid grid = new SpyingMeasureGrid(editor);
        GlobalView view = new GlobalView(new MarkerZone(editor), grid);
        grid.forgetCallsMadeWhileBuilding();

        view.moveCursorHighlight();

        assertEquals(0, grid.revalidateCalls);
        assertFalse(grid.fullRepaintCalled);
        assertEquals(1, grid.repaintedAreas.size());
    }
}
