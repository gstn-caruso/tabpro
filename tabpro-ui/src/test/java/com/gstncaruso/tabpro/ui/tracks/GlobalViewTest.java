package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.awt.Dimension;
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
