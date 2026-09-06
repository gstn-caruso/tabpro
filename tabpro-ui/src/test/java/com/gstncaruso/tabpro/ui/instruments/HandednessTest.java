package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HandednessTest {

    @Test
    void rightHandedLeavesTheCoordinateAsIs() {
        assertEquals(30, Handedness.RIGHT_HANDED.mirror(30, 900));
    }

    @Test
    void leftHandedFlipsItAroundTheTotalWidth() {
        assertEquals(870, Handedness.LEFT_HANDED.mirror(30, 900));
        assertEquals(30, Handedness.LEFT_HANDED.mirror(870, 900));
    }
}
