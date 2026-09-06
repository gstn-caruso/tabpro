package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import java.awt.Dimension;
import org.junit.jupiter.api.Test;

class GlobalViewTest {

    @Test
    void stacksTheMarkerZoneOnTopOfTheGrid() {
        Editor editor = new Editor(Score.blank());
        GlobalView view = new GlobalView(editor);

        Dimension size = view.getPreferredSize();

        assertEquals(MeasureGrid.CELL_WIDTH, size.width);
        assertEquals(MarkerZone.HEIGHT + view.grid().getPreferredSize().height, size.height);
    }
}
