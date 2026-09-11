package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class MarkerZoneTest {

    @Test
    void theAccessibleNameIsAvailableInEnglish() {
        assertEquals("Marker Zone", Texts.forLocale(Locale.ENGLISH).text("views.MarkerZone.name"));
    }

    @Test
    void paintsTheMarkerNameInWarningRedInsteadOfItsOwnColor() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        for (int i = 0; i < 5; i++) {
            editor.insertMeasure();
        }
        editor.setMarker(Marker.named("Chorus"));
        MarkerZone zone = new MarkerZone(editor);
        zone.setSize(zone.getPreferredSize());

        BufferedImage painted = paint(zone);

        assertTrue(containsColor(painted, ScoreColors.WARNING),
                "the marker name has to paint in warning red (WARNING)");
        assertFalse(containsColor(painted, new java.awt.Color(0xFF, 0, 0)),
                "it no longer paints with the marker's own color");
    }

    private static boolean containsColor(BufferedImage image, java.awt.Color color) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) == color.getRGB()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void isAsWideAsTheGridAndAsTallAsItsOwnBand() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.insertMeasure();
        editor.insertMeasure();
        MarkerZone zone = new MarkerZone(editor);

        Dimension size = zone.getPreferredSize();

        assertEquals(3 * MeasureGrid.CELL_WIDTH, size.width);
        assertEquals(MarkerZone.HEIGHT, size.height);
    }

    @Test
    void findsTheMeasureUnderThePointer() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.insertMeasure();
        editor.insertMeasure();
        MarkerZone zone = new MarkerZone(editor);

        assertEquals(0, zone.measureAt(2));
        assertEquals(1, zone.measureAt(MeasureGrid.CELL_WIDTH + 2));
        assertEquals(2, zone.measureAt(2 * MeasureGrid.CELL_WIDTH + 2));
    }

    @Test
    void hasAnAccessibleNameAndTooltip() {
        MarkerZone zone = new MarkerZone(new Editor(Score.blank(new TextsDefaultNames())));

        assertEquals("Zona de marcadores", zone.getAccessibleContext().getAccessibleName());
        assertTrue(zone.getToolTipText() != null && !zone.getToolTipText().isBlank());
    }

    @Test
    void theRightArrowKeyMovesTheCaretToTheNextMeasure() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
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
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.insertMeasure();
        MarkerZone zone = new MarkerZone(editor);
        zone.setSize(zone.getPreferredSize());
        BufferedImage withoutFocus = paint(zone);

        gainFocus(zone);
        BufferedImage withFocus = paint(zone);

        assertTrue(differsSomewhere(withoutFocus, withFocus), "the focus has to be visible in the drawing");
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
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.insertMeasure();
        editor.insertMeasure();
        MarkerZone zone = new MarkerZone(editor);
        zone.markerNamePrompt = initial -> "Chorus";
        pressShortcut(zone, KeyStroke.getKeyStroke("RIGHT"));

        pressShortcut(zone, KeyStroke.getKeyStroke("ENTER"));

        assertEquals("Chorus", editor.score().attributesOf(1).marker().orElseThrow().name());
    }
}
