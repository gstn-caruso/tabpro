package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class FretboardTypeTest {

    @Test
    void classicalHasNoInlaysUnlikeElectric() {
        assertEquals(InlayStyle.NONE, FretboardType.CLASSICAL.inlayStyle());
        assertEquals(InlayStyle.DOTS, FretboardType.ELECTRIC.inlayStyle());
    }

    @Test
    void basicUsesADifferentInlayShape() {
        assertEquals(InlayStyle.DIAMONDS, FretboardType.BASIC.inlayStyle());
    }

    @Test
    void everyTypeHasItsOwnWoodColor() {
        var colors = java.util.Set.of(
                FretboardType.ELECTRIC.woodColor(),
                FretboardType.ACOUSTIC.woodColor(),
                FretboardType.CLASSICAL.woodColor(),
                FretboardType.BASIC.woodColor());

        assertEquals(4, colors.size(), "los cuatro tipos se ven distintos");
    }

    @Test
    void aClassicalNeckIsWiderThanAnElectricOne() {
        assertNotEquals(FretboardType.ELECTRIC.neckWidthFactor(), FretboardType.CLASSICAL.neckWidthFactor());
        assertEquals(true, FretboardType.CLASSICAL.neckWidthFactor() > FretboardType.ELECTRIC.neckWidthFactor());
    }
}
