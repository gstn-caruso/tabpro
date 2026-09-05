package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import java.awt.Dimension;
import org.junit.jupiter.api.Test;

class MarkerZoneTest {

    @Test
    void isAsWideAsTheGridAndAsTallAsItsOwnBand() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.insertMeasure();
        MarkerZone zone = new MarkerZone(editor);

        Dimension size = zone.getPreferredSize();

        assertEquals(3 * MeasureGrid.CELL_WIDTH, size.width);
        assertEquals(MarkerZone.HEIGHT, size.height);
    }

    @Test
    void findsTheMeasureUnderThePointer() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.insertMeasure();
        MarkerZone zone = new MarkerZone(editor);

        assertEquals(0, zone.measureAt(2));
        assertEquals(1, zone.measureAt(MeasureGrid.CELL_WIDTH + 2));
        assertEquals(2, zone.measureAt(2 * MeasureGrid.CELL_WIDTH + 2));
    }
}
