package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
