package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import javax.accessibility.AccessibleValue;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class PotentiometerTest {

    @Test
    void theLowestValuePointsDownAndToTheLeft() {
        assertEquals(225.0, Potentiometer.angleDegrees(0, 0, 127), 0.001);
    }

    @Test
    void theHighestValuePointsDownAndToTheRight() {
        assertEquals(-45.0, Potentiometer.angleDegrees(127, 0, 127), 0.001);
    }

    @Test
    void theMiddleValuePointsStraightUp() {
        assertEquals(90.0, Potentiometer.angleDegrees(63, 0, 126), 0.001);
    }

    @Test
    void increasingTheValueSweepsClockwise() {
        double low = Potentiometer.angleDegrees(10, 0, 127);
        double high = Potentiometer.angleDegrees(100, 0, 127);

        assertTrue(high < low, "un valor mas alto tiene que apuntar mas hacia la derecha");
    }

    @Test
    void startsAtTheGivenValueAndClampsWhatItIsGiven() {
        Potentiometer knob = new Potentiometer(0, 127, 100);

        assertEquals(100, knob.getValue());

        knob.setValue(500);
        assertEquals(127, knob.getValue());

        knob.setValue(-10);
        assertEquals(0, knob.getValue());
    }

    @Test
    void settingTheValueProgrammaticallyDoesNotFireTheListener() {
        Potentiometer knob = new Potentiometer(0, 127, 64);
        boolean[] fired = {false};
        knob.onUserChange(() -> fired[0] = true);

        knob.setValue(100);

        assertEquals(false, fired[0]);
    }

    @Test
    void drawsSomethingOnTheCanvas() {
        Potentiometer knob = new Potentiometer(0, 127, 64);
        knob.setSize(24, 24);
        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        knob.paint(g);
        g.dispose();

        assertTrue(hasAnyPixel(image));
    }

    @Test
    void theRightArrowKeyIncreasesTheValueByOneStep() {
        Potentiometer knob = new Potentiometer(0, 127, 64);

        pressShortcut(knob, KeyStroke.getKeyStroke("RIGHT"));

        assertEquals(65, knob.getValue());
    }

    @Test
    void theUpArrowKeyIncreasesTheValueByOneStep() {
        Potentiometer knob = new Potentiometer(0, 127, 64);

        pressShortcut(knob, KeyStroke.getKeyStroke("UP"));

        assertEquals(65, knob.getValue());
    }

    @Test
    void theLeftArrowKeyDecreasesTheValueByOneStep() {
        Potentiometer knob = new Potentiometer(0, 127, 64);

        pressShortcut(knob, KeyStroke.getKeyStroke("LEFT"));

        assertEquals(63, knob.getValue());
    }

    @Test
    void theDownArrowKeyDecreasesTheValueByOneStep() {
        Potentiometer knob = new Potentiometer(0, 127, 64);

        pressShortcut(knob, KeyStroke.getKeyStroke("DOWN"));

        assertEquals(63, knob.getValue());
    }

    @Test
    void thePageUpKeyIncreasesTheValueByTenSteps() {
        Potentiometer knob = new Potentiometer(0, 127, 64);

        pressShortcut(knob, KeyStroke.getKeyStroke("PAGE_UP"));

        assertEquals(74, knob.getValue());
    }

    @Test
    void thePageDownKeyDecreasesTheValueByTenSteps() {
        Potentiometer knob = new Potentiometer(0, 127, 64);

        pressShortcut(knob, KeyStroke.getKeyStroke("PAGE_DOWN"));

        assertEquals(54, knob.getValue());
    }

    @Test
    void theHomeKeyGoesToTheMinimum() {
        Potentiometer knob = new Potentiometer(0, 127, 64);

        pressShortcut(knob, KeyStroke.getKeyStroke("HOME"));

        assertEquals(0, knob.getValue());
    }

    @Test
    void theEndKeyGoesToTheMaximum() {
        Potentiometer knob = new Potentiometer(0, 127, 64);

        pressShortcut(knob, KeyStroke.getKeyStroke("END"));

        assertEquals(127, knob.getValue());
    }

    @Test
    void exposesItsCurrentValueToAssistiveTechnology() {
        Potentiometer knob = new Potentiometer(0, 127, 64);

        AccessibleValue accessibleValue = (AccessibleValue) knob.getAccessibleContext();

        assertEquals(64, accessibleValue.getCurrentAccessibleValue().intValue());
        assertEquals(0, accessibleValue.getMinimumAccessibleValue().intValue());
        assertEquals(127, accessibleValue.getMaximumAccessibleValue().intValue());
    }

    @Test
    void paintsAVisibleRingWhenItGetsFocus() {
        Potentiometer knob = new Potentiometer(0, 127, 64);
        knob.setSize(24, 24);
        BufferedImage withoutFocus = paint(knob);

        gainFocus(knob);
        BufferedImage withFocus = paint(knob);

        assertTrue(differsSomewhere(withoutFocus, withFocus), "el foco tiene que verse en el dibujo");
    }

    private static void gainFocus(Potentiometer knob) {
        for (var listener : knob.getFocusListeners()) {
            listener.focusGained(new FocusEvent(knob, FocusEvent.FOCUS_GAINED));
        }
    }

    private static BufferedImage paint(Potentiometer knob) {
        BufferedImage image = new BufferedImage(knob.getWidth(), knob.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        knob.paint(g);
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

    private static void pressShortcut(JComponent component, KeyStroke keyStroke) {
        Object name = component.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name).actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
    }

    private boolean hasAnyPixel(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
