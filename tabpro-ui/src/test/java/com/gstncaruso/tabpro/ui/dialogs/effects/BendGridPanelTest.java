package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class BendGridPanelTest {

    @Test
    void hasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new BendGridPanel(BendCurveEditor.blank(BendType.BEND, 4)));
    }

    @Test
    void theRightArrowKeyMovesTheCaretForward() {
        BendGridPanel panel = new BendGridPanel(BendCurveEditor.blank(BendType.BEND, 4));

        pressShortcut(panel, KeyStroke.getKeyStroke("RIGHT"));

        assertEquals(1, panel.caretPosition());
    }

    private static void pressShortcut(JComponent component, KeyStroke keyStroke) {
        Object name = component.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name).actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
    }

    @Test
    void theLeftArrowKeyMovesTheCaretBackward() {
        BendGridPanel panel = new BendGridPanel(BendCurveEditor.blank(BendType.BEND, 4));
        pressShortcut(panel, KeyStroke.getKeyStroke("RIGHT"));
        pressShortcut(panel, KeyStroke.getKeyStroke("RIGHT"));

        pressShortcut(panel, KeyStroke.getKeyStroke("LEFT"));

        assertEquals(1, panel.caretPosition());
    }

    @Test
    void theUpArrowKeyRaisesTheCaretQuarterTones() {
        BendGridPanel panel = new BendGridPanel(BendCurveEditor.blank(BendType.BEND, 4));

        pressShortcut(panel, KeyStroke.getKeyStroke("UP"));

        assertEquals(1, panel.caretQuarterTones());
    }

    @Test
    void theDownArrowKeyLowersTheCaretQuarterTones() {
        BendGridPanel panel = new BendGridPanel(BendCurveEditor.blank(BendType.BEND, 4));
        pressShortcut(panel, KeyStroke.getKeyStroke("UP"));
        pressShortcut(panel, KeyStroke.getKeyStroke("UP"));

        pressShortcut(panel, KeyStroke.getKeyStroke("DOWN"));

        assertEquals(1, panel.caretQuarterTones());
    }

    @Test
    void theEnterKeyClicksAtTheCaretJustLikeALeftClick() {
        BendCurveEditor editor = BendCurveEditor.blank(BendType.BEND, 4);
        BendGridPanel panel = new BendGridPanel(editor);
        pressShortcut(panel, KeyStroke.getKeyStroke("RIGHT"));
        pressShortcut(panel, KeyStroke.getKeyStroke("UP"));

        pressShortcut(panel, KeyStroke.getKeyStroke("ENTER"));

        assertEquals(java.util.List.of(1, 1, 0), pointAt(editor, 1));
    }

    private static java.util.List<Integer> pointAt(BendCurveEditor editor, int position) {
        BendPoint point = editor.points().stream().filter(p -> p.position() == position).findFirst().orElseThrow();
        return java.util.List.of(point.position(), point.quarterTones(), point.vibrato());
    }

    @Test
    void theSpaceKeyAddsVibratoAtTheCaretJustLikeARightClick() {
        BendCurveEditor editor = new BendCurveEditor(java.util.List.of(BendPoint.at(0, 0), BendPoint.at(60, 4)));
        BendGridPanel panel = new BendGridPanel(editor);

        pressShortcut(panel, KeyStroke.getKeyStroke("SPACE"));

        assertEquals(1, pointAt(editor, 0).get(2));
    }

    @Test
    void paintsAVisibleCaretRingWhenItGetsFocus() {
        BendGridPanel panel = new BendGridPanel(BendCurveEditor.blank(BendType.BEND, 4));
        panel.setSize(360, 180);
        BufferedImage withoutFocus = paint(panel);

        gainFocus(panel);
        BufferedImage withFocus = paint(panel);

        assertTrue(differsSomewhere(withoutFocus, withFocus), "el foco tiene que verse en el dibujo");
    }

    private static void gainFocus(BendGridPanel panel) {
        for (var listener : panel.getFocusListeners()) {
            listener.focusGained(new FocusEvent(panel, FocusEvent.FOCUS_GAINED));
        }
    }

    private static BufferedImage paint(BendGridPanel panel) {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        panel.paint(g);
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
}
