package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DisplayModeTest {

    @Test
    void togglingSwapsBetweenKnobAndNumber() {
        assertEquals(DisplayMode.NUMBER, DisplayMode.KNOB.toggled());
        assertEquals(DisplayMode.KNOB, DisplayMode.NUMBER.toggled());
    }
}
