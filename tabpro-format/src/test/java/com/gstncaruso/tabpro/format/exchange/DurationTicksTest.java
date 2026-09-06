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
        // 900 esta a 60 de la negra (960) y a 180 de la corchea con puntillo (720).
        assertEquals(Duration.of(NoteValue.QUARTER), DurationTicks.nearestTo(900));
    }

    @Test
    void aFinestGridExcludesFiguresThatNeedAFinerSubdivision() {
        // sin restriccion, 700 tics redondea a la corchea con puntillo (720, a 20): es 1.5
        // corcheas, y hace falta una grilla de semicorchea para llegar justo a ella. Pidiendo que
        // la grilla no sea mas fina que la corchea, esa figura queda afuera y gana la corchea
        // simple (480, a 220), la mas cercana que la grilla permite.
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
        // 900 no es ninguna figura simple ni con puntillo: negra (960) + corchea (480) se pasan,
        // asi que se arma con corchea (480) + semicorchea (240) + fusa (120) + semifusa (60) = 900.
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
