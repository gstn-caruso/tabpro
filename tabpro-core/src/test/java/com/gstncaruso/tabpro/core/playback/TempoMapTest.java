package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class TempoMapTest {

    private static final long QUARTER = Duration.TICKS_PER_QUARTER;

    @Test
    void aScoreWithoutTempoChangesSoundsAtTheSameSpeedFromEndToEnd() {
        TempoMap steady = TempoMap.steady(120);

        assertEquals(120, steady.bpmAt(0));
        assertEquals(120, steady.bpmAt(100 * QUARTER));
        assertEquals(1, steady.changes().size());
    }

    @Test
    void aTempoChangeRulesFromItsOwnTickOnwards() {
        TempoMap map = TempoMap.steady(120).changingTo(4 * QUARTER, 90);

        assertEquals(120, map.bpmAt(4 * QUARTER - 1));
        assertEquals(90, map.bpmAt(4 * QUARTER));
        assertEquals(90, map.bpmAt(40 * QUARTER));
    }

    @Test
    void everyTempoChangeRulesUntilTheNextOne() {
        TempoMap map = TempoMap.steady(120).changingTo(4 * QUARTER, 90).changingTo(8 * QUARTER, 200);

        assertEquals(120, map.bpmAt(0));
        assertEquals(90, map.bpmAt(6 * QUARTER));
        assertEquals(200, map.bpmAt(8 * QUARTER));
    }

    @Test
    void theRealTimeOfATickIsTheSumOfTheStretchesItCrosses() {
        TempoMap map = TempoMap.steady(120).changingTo(2 * QUARTER, 60);

        assertEquals(1.0, map.secondsAt(2 * QUARTER), 1e-9, "dos negras a 120 duran un segundo");
        assertEquals(3.0, map.secondsAt(4 * QUARTER), 1e-9, "y dos negras mas a 60 suman dos segundos");
    }

    @Test
    void theRealTimeOfASteadyScoreIsJustTicksOverTempo() {
        assertEquals(2.0, TempoMap.steady(120).secondsAt(4 * QUARTER), 1e-9);
    }

    @Test
    void repeatingTheTempoThatIsAlreadySoundingDoesNotOpenANewStretch() {
        TempoMap map = TempoMap.steady(120).changingTo(4 * QUARTER, 120);

        assertEquals(1, map.changes().size());
    }

    @Test
    void twoTemposAskedForTheSameTickLeaveTheLastOne() {
        TempoMap map = TempoMap.steady(120).changingTo(4 * QUARTER, 90).changingTo(4 * QUARTER, 200);

        assertEquals(200, map.bpmAt(4 * QUARTER));
        assertEquals(2, map.changes().size());
    }

    @Test
    void changingTheStartingTempoDragsTheWholeMapWithIt() {
        TempoMap map = TempoMap.steady(120).changingTo(4 * QUARTER, 60).startingAt(60);

        assertEquals(60, map.bpmAt(0));
        assertEquals(30, map.bpmAt(4 * QUARTER));
    }

    @Test
    void aMapNeverFallsBelowOneBeatPerMinute() {
        assertEquals(1, TempoMap.steady(2).startingAt(1).bpmAt(0));
    }

    @Test
    void delayingTheMusicLeavesTheStartingTempoCoveringTheCountIn() {
        TempoMap map = TempoMap.steady(120).changingTo(4 * QUARTER, 90).shiftedBy(4 * QUARTER);

        assertEquals(120, map.bpmAt(0));
        assertEquals(120, map.bpmAt(4 * QUARTER));
        assertEquals(90, map.bpmAt(8 * QUARTER));
    }

    @Test
    void aMapAlwaysSaysAtWhatSpeedTheMusicStarts() {
        assertThrows(IllegalArgumentException.class, () -> new TempoMap(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new TempoMap(List.of(new TempoChange(480, 120))));
    }

    @Test
    void theStretchesOfAMapAreInTheOrderTheySound() {
        assertThrows(IllegalArgumentException.class, () -> new TempoMap(
                List.of(new TempoChange(0, 120), new TempoChange(960, 90), new TempoChange(480, 200))));
    }

    @Test
    void aStretchNeedsARealTempo() {
        assertThrows(IllegalArgumentException.class, () -> new TempoChange(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new TempoChange(-1, 120));
    }

    @Test
    void aSteadyMapKnowsItHasNothingToAnnounceMidWay() {
        assertTrue(TempoMap.steady(120).isSteady());
        assertTrue(!TempoMap.steady(120).changingTo(QUARTER, 90).isSteady());
    }
}
