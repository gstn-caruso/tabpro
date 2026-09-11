package com.gstncaruso.tabpro.ui.percussion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class PercussionSoundPaletteTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new PercussionSoundPalette(sound -> { }, sound -> { }));
    }

    @Test
    void aSoundCellShowsItsNumberAndItsSpanishName() {
        PercussionSoundPalette palette = new PercussionSoundPalette(sound -> { }, sound -> { });

        assertEquals("35 — Bombo acústico", Combos.renderedTextOfList(palette.soundList(), 35));
    }

    @Test
    void aSingleClickPlaysTheSound() {
        List<Integer> played = new ArrayList<>();
        List<Integer> added = new ArrayList<>();
        PercussionSoundPalette palette = new PercussionSoundPalette(played::add, added::add);
        JList<Integer> list = sized(palette);

        click(list, 0, 1);

        assertEquals(List.of(list.getModel().getElementAt(0)), played);
        assertEquals(List.of(), added);
    }

    @Test
    void aDoubleClickAddsTheSoundInstead() {
        List<Integer> played = new ArrayList<>();
        List<Integer> added = new ArrayList<>();
        PercussionSoundPalette palette = new PercussionSoundPalette(played::add, added::add);
        JList<Integer> list = sized(palette);

        click(list, 0, 2);

        assertEquals(List.of(list.getModel().getElementAt(0)), added);
    }

    @Test
    void theGridHasFourColumnsInGP5Order() {
        PercussionSoundPalette palette = new PercussionSoundPalette(sound -> { }, sound -> { });
        JList<Integer> list = sized(palette);

        List<Integer> xPerIndex = new ArrayList<>();
        List<Integer> yPerIndex = new ArrayList<>();
        for (int index = 0; index < list.getModel().getSize(); index++) {
            Rectangle bounds = list.getCellBounds(index, index);
            xPerIndex.add(bounds.x);
            yPerIndex.add(bounds.y);
        }

        assertEquals(4, xPerIndex.stream().distinct().count(), "the grid does not have four columns");
        assertEquals(xPerIndex.get(0), xPerIndex.get(1), "the first and second sounds are not in the same column");
        assertTrue(yPerIndex.get(1) > yPerIndex.get(0), "the second sound is not below the first, as in GP5");
    }

    @Test
    void theCellSizeStaysFixedFromConstructionAndIsNotMeasuredOnEveryLayout() {
        PercussionSoundPalette palette = new PercussionSoundPalette(sound -> { }, sound -> { });
        JList<Integer> list = palette.soundList();

        assertTrue(list.getFixedCellHeight() > 0,
                "row height should stay fixed from construction, not measured on every layout");
        assertTrue(list.getFixedCellWidth() > 0,
                "column width should stay fixed from construction, not measured on every layout");
    }

    private static final int CELL_HEIGHT = 20;

    @Test
    void theCellTheTestMeasuresIsTheCellTheListResolvesTheClickInto() {
        PercussionSoundPalette palette = new PercussionSoundPalette(sound -> { }, sound -> { });
        JList<Integer> list = sized(palette);

        for (int index = 0; index < 3; index++) {
            Rectangle bounds = list.getCellBounds(index, index);
            Point center = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);

            assertEquals(index, list.locationToIndex(center), "row " + index + " does not resolve to itself");
        }
    }

    private static JList<Integer> sized(PercussionSoundPalette palette) {
        palette.setSize(220, 400);
        JList<Integer> list = palette.soundList();
        list.setFixedCellHeight(CELL_HEIGHT);
        list.setSize(220, 400);
        list.doLayout();
        return list;
    }

    private static void click(JList<Integer> list, int index, int clickCount) {
        Rectangle bounds = list.getCellBounds(index, index);
        Point center = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        MouseEvent event = new MouseEvent(list, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                center.x, center.y, clickCount, false);
        for (var listener : list.getMouseListeners()) {
            listener.mouseClicked(event);
        }
    }
}
