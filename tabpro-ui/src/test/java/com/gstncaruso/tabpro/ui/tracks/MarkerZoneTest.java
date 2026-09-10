package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
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

    @Test
    void paintsAVisibleCaretRingWhenItGetsFocus() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        MarkerZone zone = new MarkerZone(editor);
        zone.setSize(zone.getPreferredSize());
        BufferedImage withoutFocus = paint(zone);

        gainFocus(zone);
        BufferedImage withFocus = paint(zone);

        assertTrue(differsSomewhere(withoutFocus, withFocus), "el foco tiene que verse en el dibujo");
    }

    private static void gainFocus(MarkerZone zone) {
        for (var listener : zone.getFocusListeners()) {
            listener.focusGained(new FocusEvent(zone, FocusEvent.FOCUS_GAINED));
        }
    }

    private static BufferedImage paint(MarkerZone zone) {
        BufferedImage image = new BufferedImage(zone.getWidth(), zone.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        zone.paint(g);
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

    @Test
    void enterEditsTheMarkerAtTheCaretJustLikeADoubleClick() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.insertMeasure();
        MarkerZone zone = new MarkerZone(editor);
        zone.markerNamePrompt = initial -> "Estribillo";
        pressShortcut(zone, KeyStroke.getKeyStroke("RIGHT"));

        pressShortcut(zone, KeyStroke.getKeyStroke("ENTER"));

        assertEquals("Estribillo", editor.score().attributesOf(1).marker().orElseThrow().name());
    }
}
