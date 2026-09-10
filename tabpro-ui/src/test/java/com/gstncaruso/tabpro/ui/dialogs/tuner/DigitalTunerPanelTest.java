package com.gstncaruso.tabpro.ui.dialogs.tuner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class DigitalTunerPanelTest {

    @Test
    void hasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new DigitalTunerPanel(new Pitch(64)));
    }

    @Test
    void startsPerfectlyInTune() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));

        assertEquals(0, panel.deviationCents());
        assertTrue(panel.isInTune());
    }

    @Test
    void clampsTheDeviationToTheDialRange() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));

        panel.setDeviationCents(500);
        assertEquals(DigitalTunerPanel.MAX_CENTS, panel.deviationCents());

        panel.setDeviationCents(-500);
        assertEquals(-DigitalTunerPanel.MAX_CENTS, panel.deviationCents());
    }

    @Test
    void aBigDeviationIsNotInTune() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));

        panel.setDeviationCents(20);

        assertFalse(panel.isInTune());
    }

    @Test
    void theNeedleLeansTowardTheSideOfTheDeviation() {
        double sharp = DigitalTunerPanel.needleAngleRadians(25);
        double flat = DigitalTunerPanel.needleAngleRadians(-25);
        double centered = DigitalTunerPanel.needleAngleRadians(0);

        assertEquals(0, centered);
        assertTrue(sharp > 0);
        assertTrue(flat < 0);
        assertEquals(-sharp, flat);
    }

    @Test
    void changingTargetIsReflected() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));

        panel.setTarget(new Pitch(69));

        assertEquals(new Pitch(69), panel.target());
    }

    @Test
    void paintsAVisibleRingWhenItGetsFocus() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));
        panel.setSize(220, 140);
        BufferedImage withoutFocus = paint(panel);

        gainFocus(panel);
        BufferedImage withFocus = paint(panel);

        assertTrue(differsSomewhere(withoutFocus, withFocus), "el foco tiene que verse en el dibujo");
    }

    private static void gainFocus(DigitalTunerPanel panel) {
        for (var listener : panel.getFocusListeners()) {
            listener.focusGained(new FocusEvent(panel, FocusEvent.FOCUS_GAINED));
        }
    }

    private static BufferedImage paint(DigitalTunerPanel panel) {
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

    @Test
    void theAccessibleDescriptionStatesHowFarItDeviates() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));

        panel.setDeviationCents(-12);

        assertEquals("E4, 12 centésimas grave", panel.getAccessibleContext().getAccessibleDescription());
    }
}
