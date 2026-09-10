package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import java.util.List;
import org.junit.jupiter.api.Test;

class PitchTrajectoryTest {

    @Test
    void aFlatCurveDoesNotMoveThePitch() {
        PitchTrajectory flat = PitchTrajectory.flat();

        assertEquals(0.0, flat.semitonesAt(0));
        assertEquals(0.0, flat.semitonesAt(500));
    }

    @Test
    void beforeTheFirstPointItIsWorthWhatTheFirstIsWorth() {
        PitchTrajectory trajectory = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(100, 2.0), new PitchTrajectory.Point(200, 0.0)));

        assertEquals(2.0, trajectory.semitonesAt(0));
    }

    @Test
    void afterTheLastPointItIsWorthWhatTheLastIsWorth() {
        PitchTrajectory trajectory = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(0, 0.0), new PitchTrajectory.Point(100, 2.0)));

        assertEquals(2.0, trajectory.semitonesAt(1000));
    }

    @Test
    void interpolatesLinearlyBetweenTwoPoints() {
        PitchTrajectory trajectory = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(0, 0.0), new PitchTrajectory.Point(100, 2.0)));

        assertEquals(1.0, trajectory.semitonesAt(50));
    }

    @Test
    void isBuiltFromABendScalingPositionsToTheNoteTicks() {
        Bend bend = Bend.of(BendType.BEND, 4);
        PitchTrajectory trajectory = PitchTrajectory.of(bend, 960);

        assertEquals(0.0, trajectory.semitonesAt(0));
        assertEquals(2.0, trajectory.semitonesAt(960));
    }

    @Test
    void aWhammyBarPlaysWithTheSameMechanismAsTheBendButCanGoDown() {
        Bend dive = Bend.of(BendType.DIVE, 4);
        PitchTrajectory trajectory = PitchTrajectory.of(dive, 960);

        assertEquals(0.0, trajectory.semitonesAt(0));
        assertEquals(-2.0, trajectory.semitonesAt(960));
    }

    @Test
    void anInstantJumpDoesNotInterpolateBetweenBeforeAndAfter() {
        PitchTrajectory trajectory = PitchTrajectory.flat()
                .withJumpAt(500, 5.0);

        assertEquals(0.0, trajectory.semitonesAt(499));
        assertEquals(5.0, trajectory.semitonesAt(500));
        assertEquals(5.0, trajectory.semitonesAt(600));
    }

    @Test
    void rampingToGraduallyReachesTheRequestedValue() {
        PitchTrajectory trajectory = PitchTrajectory.flat().rampingTo(1000, 2.0, 100);

        assertEquals(0.0, trajectory.semitonesAt(899));
        assertEquals(1.0, trajectory.semitonesAt(950));
        assertEquals(2.0, trajectory.semitonesAt(1000));
        assertEquals(2.0, trajectory.semitonesAt(2000));
    }

    @Test
    void plusAddsTwoCurvesAtEveryPointEitherOneDefines() {
        PitchTrajectory a = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(0, 0.0), new PitchTrajectory.Point(100, 2.0)));
        PitchTrajectory b = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(0, 1.0), new PitchTrajectory.Point(100, 1.0)));

        PitchTrajectory combined = a.plus(b);

        assertEquals(1.0, combined.semitonesAt(0));
        assertEquals(3.0, combined.semitonesAt(100));
    }

    @Test
    void aVibratoOscillatesAroundZero() {
        PitchTrajectory vibrato = PitchTrajectory.vibrato(960, 0.5, 240);

        assertTrue(vibrato.semitonesAt(0) <= 0.0001);
        boolean hasSomePositivePoint = false;
        boolean hasSomeNegativePoint = false;
        for (long tick = 0; tick <= 960; tick += 60) {
            double value = vibrato.semitonesAt(tick);
            if (value > 0) {
                hasSomePositivePoint = true;
            }
            if (value < 0) {
                hasSomeNegativePoint = true;
            }
        }
        assertTrue(hasSomePositivePoint && hasSomeNegativePoint);
    }

    @Test
    void aFlatCurveAlwaysStaysWithinAnyLimit() {
        assertTrue(PitchTrajectory.flat().staysWithin(0.0));
    }

    @Test
    void aCurveExactlyAtTheLimitStaysWithin() {
        PitchTrajectory trajectory = PitchTrajectory.ramp(0, 0.0, 100, 2.0);

        assertTrue(trajectory.staysWithin(2.0));
    }

    @Test
    void aCurveThatExceedsTheLimitDoesNotStayWithin() {
        PitchTrajectory trajectory = PitchTrajectory.ramp(0, 0.0, 100, 2.5);

        assertTrue(!trajectory.staysWithin(2.0));
    }

    @Test
    void theLimitLooksAtTheAbsoluteValueOfTheVariation() {
        PitchTrajectory trajectory = PitchTrajectory.ramp(0, 0.0, 100, -3.0);

        assertTrue(!trajectory.staysWithin(2.0));
    }

    @Test
    void aPointWithVibratoOscillatesThePitchForTheDurationOfItsSegment() {
        Bend withVibrato = new Bend(BendType.PREBEND, List.of(
                new BendPoint(0, 4, 2), new BendPoint(BendPoint.LAST_POSITION, 4, 0)));

        PitchTrajectory curve = PitchTrajectory.of(withVibrato, 960);

        assertTrue(maxBetween(curve, 0, 960) > 2.0, "la vibrada tiene que pasar por encima de la altura del punto");
        assertTrue(minBetween(curve, 0, 960) < 2.0, "la vibrada tiene que pasar por debajo de la altura del punto");
    }

    @Test
    void aPointWithoutVibratoKeepsThePitchStill() {
        Bend still = new Bend(BendType.PREBEND, List.of(
                new BendPoint(0, 4, 0), new BendPoint(BendPoint.LAST_POSITION, 4, 0)));

        PitchTrajectory curve = PitchTrajectory.of(still, 960);

        assertEquals(2.0, maxBetween(curve, 0, 960));
        assertEquals(2.0, minBetween(curve, 0, 960));
    }

    @Test
    void theHigherTheVibratoLevelTheMoreThePitchDeparts() {
        Bend soft = new Bend(BendType.PREBEND, List.of(
                new BendPoint(0, 4, 1), new BendPoint(BendPoint.LAST_POSITION, 4, 0)));
        Bend strong = new Bend(BendType.PREBEND, List.of(
                new BendPoint(0, 4, 3), new BendPoint(BendPoint.LAST_POSITION, 4, 0)));

        double softDeparture = maxBetween(PitchTrajectory.of(soft, 960), 0, 960);
        double strongDeparture = maxBetween(PitchTrajectory.of(strong, 960), 0, 960);

        assertTrue(strongDeparture > softDeparture, "tres niveles de vibrada tienen que apartarse mas que uno");
    }

    @Test
    void aPointsVibratoEndsWhereTheNextPointBegins() {
        Bend onlyAtTheStart = new Bend(BendType.PREBEND, List.of(
                new BendPoint(0, 4, 3),
                new BendPoint(BendPoint.LAST_POSITION / 2, 4, 0),
                new BendPoint(BendPoint.LAST_POSITION, 4, 0)));

        PitchTrajectory curve = PitchTrajectory.of(onlyAtTheStart, 960);

        assertTrue(maxBetween(curve, 0, 470) > 2.0, "el primer tramo vibra");
        assertEquals(2.0, maxBetween(curve, 490, 960), "el tramo sin vibrada queda quieto");
        assertEquals(2.0, minBetween(curve, 490, 960), "el tramo sin vibrada queda quieto");
    }

    private static double maxBetween(PitchTrajectory curve, long from, long to) {
        double max = curve.semitonesAt(from);
        for (long tick = from; tick <= to; tick++) {
            max = Math.max(max, curve.semitonesAt(tick));
        }
        return max;
    }

    private static double minBetween(PitchTrajectory curve, long from, long to) {
        double min = curve.semitonesAt(from);
        for (long tick = from; tick <= to; tick++) {
            min = Math.min(min, curve.semitonesAt(tick));
        }
        return min;
    }
}
