package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
class TrackPanelAlignmentTest {

    private static final int WIDTH = 1300;
    private static final int HEIGHT = 180;
    private static final int STRIPE_WIDTH = 4;

    @Test
    void everyTrackSquareSitsAtTheHeightOfItsOwnRowInTheMixer() {
        TrackPanel panel = panelWithTwoTracksPlayingTheFirstMeasure();
        BufferedImage painted = paint(panel);

        for (int track = 0; track < 2; track++) {
            Band row = bandOf(painted, TrackColors.of(track), 0, STRIPE_WIDTH);
            Band square = bandOf(painted, TrackColors.of(track), MixTable.WIDTH, WIDTH);

            assertTrue(square.isInside(row),
                    "track " + (track + 1) + "'s square has to fall inside its mixer row:"
                            + " row " + row + ", square " + square);
            assertTrue(Math.abs(row.center() - square.center()) <= 1,
                    "track " + (track + 1) + "'s square has to be centered in its mixer row:"
                            + " row " + row + ", square " + square);
        }
    }

    @Test
    void givenDoubleUiScale_whenLayingOutTrackPanel_thenMixerRowsAndMeasureHitsStayAligned() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Object previousScale = UIManager.get("laf.scaleFactor");
            try {
                UIManager.put("laf.scaleFactor", 2f);
                Editor editor = editorWithTwoTracksPlayingTheFirstMeasure();
                TrackPanel panel = new TrackPanel(editor);
                panel.setSize(WIDTH, HEIGHT * 2);
                layOut(panel);
                MixTable mixer = (MixTable) SwingUtilities.getAncestorOfClass(
                        MixTable.class, panel.rows().getFirst());
                JLabel headerLabel = mixer.columnTitleLabels().getFirst();
                MeasureGrid grid = (MeasureGrid) panel.gridComponent();
                List<Executable> observations = new ArrayList<>();
                observations.add(() -> assertEquals(88, headerLabel.getParent().getHeight(),
                        "double-scale mixer header height"));
                observations.add(() -> assertEquals(20f, headerLabel.getFont().getSize2D(),
                        "double-scale column title font"));
                for (int track = 0; track < 2; track++) {
                    MixTableRow row = panel.rows().get(track);
                    Rectangle cell = grid.cellBounds(track, 0);
                    Point rowTop = SwingUtilities.convertPoint(row, 0, 0, panel);
                    Point cellTop = SwingUtilities.convertPoint(grid, cell.x, cell.y, panel);
                    observations.add(() -> assertAll("row " + row.numberLabel().getText(),
                            () -> assertEquals(68, row.getHeight(), "double-scale mixer row height"),
                            () -> assertEquals(20f, row.numberLabel().getFont().getSize2D(),
                                    "double-scale row number font"),
                            () -> assertEquals(rowTop.y, cellTop.y, "grid and mixer row top"),
                            () -> assertEquals(68, cell.height, "double-scale grid row height"),
                            () -> assertEquals(30, cell.width, "double-scale measure cell width")));
                }
                Point headerTop = SwingUtilities.convertPoint(headerLabel.getParent(), 0, 0, grid);
                int secondRowCenter = headerTop.y + 88 + 68 + 34;
                grid.dispatchEvent(new MouseEvent(grid, MouseEvent.MOUSE_PRESSED,
                        System.currentTimeMillis(), 0, 15, secondRowCenter, 1, false));
                observations.add(() -> assertEquals(1, editor.cursor().track(),
                        "click beside the center of the double-scale second mixer row selects that track"));
                observations.add(() -> assertEquals(0, editor.cursor().measure(),
                        "click inside the double-scale first measure stays in that measure"));
                assertAll(observations);
            } finally {
                UIManager.put("laf.scaleFactor", previousScale);
            }
        });
    }

    @Test
    void givenNarrowScaledTrackPanel_whenScrollingMixer_thenAllEffectsAndMeasureGridRemainReachable()
            throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Object previousScale = UIManager.get("laf.scaleFactor");
            try {
                UIManager.put("laf.scaleFactor", 2f);
                TrackPanel panel = panelWithTwoTracksPlayingTheFirstMeasure();
                panel.setSize(WIDTH, panel.getPreferredSize().height);
                layOut(panel);
                MixTableRow row = panel.rows().getFirst();
                JScrollPane mixer = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, row);
                MeasureGrid grid = (MeasureGrid) panel.gridComponent();
                JScrollPane measures = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, grid);
                JScrollBar horizontal = mixer.getHorizontalScrollBar();
                horizontal.setValue(horizontal.getMaximum() - horizontal.getVisibleAmount());
                layOut(panel);
                ParameterCell lastEffect = row.parameterCells().getLast();
                Rectangle effectBounds = SwingUtilities.convertRectangle(
                        lastEffect.getParent(), lastEffect.getBounds(), mixer.getViewport().getView());
                int measureWidth = grid.cellBounds(0, 0).width;

                assertAll("scaled mixer and measure grid remain reachable in a narrow panel",
                        () -> assertTrue(new Rectangle(0, 0, panel.getWidth(), panel.getHeight())
                                        .contains(mixer.getBounds()),
                                "mixer scroll pane must stay inside the panel: panel=" + panel.getBounds()
                                        + ", mixer=" + mixer.getBounds()),
                        () -> assertTrue(measures.getViewport().getExtentSize().width >= 2 * measureWidth,
                                "measure viewport must leave room for multiple measures: width="
                                        + measures.getViewport().getExtentSize().width
                                        + ", measure width=" + measureWidth),
                        () -> assertTrue(horizontal.isVisible(),
                                "overflowing mixer columns need a visible horizontal scrollbar"),
                        () -> assertTrue(horizontal.getMaximum() > horizontal.getVisibleAmount(),
                                "horizontal scrolling must offer a nonzero range"),
                        () -> assertTrue(lastEffect.isVisible()
                                        && mixer.getViewport().getViewRect().contains(effectBounds),
                                "scrolling to the end must expose the whole last effect: viewport="
                                        + mixer.getViewport().getViewRect() + ", effect=" + effectBounds));
            } finally {
                UIManager.put("laf.scaleFactor", previousScale);
            }
        });
    }

    private static TrackPanel panelWithTwoTracksPlayingTheFirstMeasure() {
        return new TrackPanel(editorWithTwoTracksPlayingTheFirstMeasure());
    }

    private static Editor editorWithTwoTracksPlayingTheFirstMeasure() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.addTrack(Track.standardBass("Bajo"));
        for (int track = 0; track < 2; track++) {
            editor.selectTrack(track);
            editor.moveTo(0, 0, 1);
            editor.setFret(3);
        }
        editor.selectTrack(0);
        return editor;
    }

    private static BufferedImage paint(TrackPanel panel) {
        panel.setSize(WIDTH, HEIGHT);
        layOut(panel);
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        panel.paint(g);
        g.dispose();
        return image;
    }

    private static void layOut(Component component) {
        if (component instanceof Container container) {
            container.doLayout();
            for (Component child : container.getComponents()) {
                layOut(child);
            }
        }
    }

    private static Band bandOf(BufferedImage painted, Color color, int fromX, int toX) {
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;
        for (int x = fromX; x < Math.min(toX, painted.getWidth()); x++) {
            for (int y = 0; y < painted.getHeight(); y++) {
                if (painted.getRGB(x, y) == color.getRGB()) {
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        if (bottom < top) {
            return fail("no pixel " + color + " was painted between x=" + fromX + " and x=" + toX);
        }
        return new Band(top, bottom);
    }

    private record Band(int top, int bottom) {

        boolean isInside(Band other) {
            return top >= other.top && bottom <= other.bottom;
        }

        int center() {
            return (top + bottom) / 2;
        }

        @Override
        public String toString() {
            return "y=" + top + ".." + bottom;
        }
    }
}
