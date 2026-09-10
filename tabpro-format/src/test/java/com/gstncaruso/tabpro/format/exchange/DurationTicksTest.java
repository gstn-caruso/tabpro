package com.gstncaruso.tabpro.format.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class DurationTicksTest {

    @Test
    void findsTheExactValueForAClockTickCount() {
        assertEquals(Duration.of(NoteValue.QUARTER), DurationTicks.nearestTo(960));
        assertEquals(new Duration(NoteValue.QUARTER, true), DurationTicks.nearestTo(1440));
    }

    @Test
    void roundsToTheClosestRepresentableValue() {
        assertEquals(Duration.of(NoteValue.QUARTER), DurationTicks.nearestTo(900));
    }

    @Test
    void aFinestGridExcludesFiguresThatNeedAFinerSubdivision() {
        assertEquals(new Duration(NoteValue.EIGHTH, true), DurationTicks.nearestTo(700));
        assertEquals(Duration.of(NoteValue.EIGHTH), DurationTicks.nearestTo(700, NoteValue.EIGHTH));
    }

    @Test
    void decomposesAnExactValueIntoASingleDuration() {
        assertEquals(List.of(Duration.of(NoteValue.WHOLE)), DurationTicks.decompose(3840));
        assertEquals(List.of(new Duration(NoteValue.QUARTER, true)), DurationTicks.decompose(1440));
    }

    @Test
    void decomposesAnAwkwardLengthIntoSeveralDurations() {
        assertEquals(
                List.of(
                        Duration.of(NoteValue.EIGHTH),
                        Duration.of(NoteValue.SIXTEENTH),
                        Duration.of(NoteValue.THIRTY_SECOND),
                        Duration.of(NoteValue.SIXTY_FOURTH)),
                DurationTicks.decompose(900));
    }

    @Test
    void decomposesZeroIntoNothing() {
        assertEquals(List.of(), DurationTicks.decompose(0));
    }
}
