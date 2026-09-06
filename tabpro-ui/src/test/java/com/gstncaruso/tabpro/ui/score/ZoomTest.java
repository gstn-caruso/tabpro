package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ZoomTest {

    @Test
    void wholeIsOneHundredPercent() {
        assertEquals(100, Zoom.whole().percent());
        assertEquals(1.0, Zoom.whole().factor());
    }

    @Test
    void clampsBelowTheMinimum() {
        assertEquals(Zoom.MIN_PERCENT, new Zoom(0).percent());
        assertEquals(Zoom.MIN_PERCENT, new Zoom(-50).percent());
    }

    @Test
    void clampsAboveTheMaximum() {
        assertEquals(Zoom.MAX_PERCENT, new Zoom(500).percent());
    }

    @Test
    void inStepsUpAndOutStepsDown() {
        Zoom zoom = Zoom.whole();
        assertEquals(110, zoom.in().percent());
        assertEquals(90, zoom.out().percent());
    }

    @Test
    void inStopsAtTheMaximum() {
        assertTrue(new Zoom(Zoom.MAX_PERCENT).in().isAtMaximum());
    }

    @Test
    void outStopsAtTheMinimum() {
        assertTrue(new Zoom(Zoom.MIN_PERCENT).out().isAtMinimum());
    }
}
