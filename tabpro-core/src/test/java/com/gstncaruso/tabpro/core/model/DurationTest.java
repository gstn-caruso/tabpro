package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DurationTest {

    @Test
    void quarterLastsTicksPerQuarter() {
        assertEquals(Duration.TICKS_PER_QUARTER, Duration.quarter().ticks());
    }

    @Test
    void wholeLastsFourQuarters() {
        Duration whole = new Duration(NoteValue.WHOLE, false);
        assertEquals(Duration.TICKS_PER_QUARTER * 4L, whole.ticks());
    }

    @Test
    void sixtyFourthIsTheShortest() {
        assertEquals(NoteValue.SIXTY_FOURTH, NoteValue.SIXTY_FOURTH.shorter());
    }

    @Test
    void dotAddsHalfOfTheValue() {
        Duration dottedQuarter = new Duration(NoteValue.QUARTER, true);
        assertEquals(Duration.TICKS_PER_QUARTER * 3L / 2, dottedQuarter.ticks());
    }

    @Test
    void lengthenedQuarterIsAHalf() {
        assertEquals(NoteValue.HALF, Duration.quarter().longer().value());
    }

    @Test
    void lengtheningAWholeStaysWhole() {
        Duration whole = new Duration(NoteValue.WHOLE, false);
        assertEquals(NoteValue.WHOLE, whole.longer().value());
    }

    @Test
    void shortenedQuarterIsAnEighth() {
        assertEquals(NoteValue.EIGHTH, Duration.quarter().shorter().value());
    }

    @Test
    void shorteningASixtyFourthStaysSixtyFourth() {
        Duration sixtyFourth = new Duration(NoteValue.SIXTY_FOURTH, false);
        assertEquals(NoteValue.SIXTY_FOURTH, sixtyFourth.shorter().value());
    }

    @Test
    void togglingTheDotTwiceRestoresTheDuration() {
        Duration quarter = Duration.quarter();
        assertEquals(quarter, quarter.toggledDot().toggledDot());
    }
}
