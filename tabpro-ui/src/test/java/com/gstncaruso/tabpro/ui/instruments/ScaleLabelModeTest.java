package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScaleLabelModeTest {

    private final Scale cMajor = Scale.cMajor();

    @Test
    void nameKeepsWhateverNameTheFretboardAlreadyComputed() {
        assertEquals("Mi", ScaleLabelMode.NAME.textFor("Mi", 64, cMajor));
    }

    @Test
    void intervalIgnoresTheGivenNameAndAsksTheScale() {
        assertEquals("3", ScaleLabelMode.INTERVAL.textFor("Mi", 64, cMajor));
    }

    @Test
    void degreeCountsThePositionWithinTheScale() {
        assertEquals("3", ScaleLabelMode.DEGREE.textFor("Mi", 64, cMajor));
    }

    @Test
    void degreeAndIntervalDifferOnAScaleThatSkipsSteps() {
        Scale minorPentatonic = new Scale(0, ScaleType.MINOR_PENTATONIC);

        assertEquals("b3", ScaleLabelMode.INTERVAL.textFor("Re#", 63, minorPentatonic));
        assertEquals("2", ScaleLabelMode.DEGREE.textFor("Re#", 63, minorPentatonic));
    }
}
