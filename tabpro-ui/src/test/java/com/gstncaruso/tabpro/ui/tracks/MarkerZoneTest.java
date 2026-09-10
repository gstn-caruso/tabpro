package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
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

    @Test
    void tieneNombreYTooltipAccesibles() {
        MarkerZone zone = new MarkerZone(new Editor(Score.blank()));

        assertEquals("Zona de marcadores", zone.getAccessibleContext().getAccessibleName());
        assertTrue(zone.getToolTipText() != null && !zone.getToolTipText().isBlank());
    }

    @Test
    void theRightArrowKeyMovesTheCaretToTheNextMeasure() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.insertMeasure();
        MarkerZone zone = new MarkerZone(editor);

        pressShortcut(zone, KeyStroke.getKeyStroke("RIGHT"));

        assertEquals(1, zone.caret());
    }

    private static void pressShortcut(JComponent component, KeyStroke keyStroke) {
        Object name = component.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name).actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
    }
}
