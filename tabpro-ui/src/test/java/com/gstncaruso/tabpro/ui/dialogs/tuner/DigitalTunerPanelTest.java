package com.gstncaruso.tabpro.ui.dialogs.tuner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import org.junit.jupiter.api.Test;

class DigitalTunerPanelTest {

    @Test
    void startsPerfectlyInTune() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));

        assertEquals(0, panel.deviationCents());
        assertTrue(panel.isInTune());
    }

    @Test
    void clampsTheDeviationToTheDialRange() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));

        panel.setDeviationCents(500);
        assertEquals(DigitalTunerPanel.MAX_CENTS, panel.deviationCents());

        panel.setDeviationCents(-500);
        assertEquals(-DigitalTunerPanel.MAX_CENTS, panel.deviationCents());
    }

    @Test
    void aBigDeviationIsNotInTune() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));

        panel.setDeviationCents(20);

        assertFalse(panel.isInTune());
    }

    @Test
    void theNeedleLeansTowardTheSideOfTheDeviation() {
        double sharp = DigitalTunerPanel.needleAngleRadians(25);
        double flat = DigitalTunerPanel.needleAngleRadians(-25);
        double centered = DigitalTunerPanel.needleAngleRadians(0);

        assertEquals(0, centered);
        assertTrue(sharp > 0);
        assertTrue(flat < 0);
        assertEquals(-sharp, flat);
    }

    @Test
    void changingTargetIsReflected() {
        DigitalTunerPanel panel = new DigitalTunerPanel(new Pitch(64));

        panel.setTarget(new Pitch(69));

        assertEquals(new Pitch(69), panel.target());
    }
}
