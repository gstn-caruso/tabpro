package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleValue;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class LevelSliderTest {

    @Test
    void startsAtTheGivenValueAndClampsWhatItIsGiven() {
        LevelSlider slider = new LevelSlider(0, 127, 100, Color.ORANGE, Color.GRAY);

        assertEquals(100, slider.getValue());

        slider.setValue(500);
        assertEquals(127, slider.getValue());

        slider.setValue(-10);
        assertEquals(0, slider.getValue());
    }

    @Test
    void settingTheValueProgrammaticallyDoesNotFireTheListener() {
        LevelSlider slider = new LevelSlider(0, 127, 64, Color.ORANGE, Color.GRAY);
        boolean[] fired = {false};
        slider.onUserChange(() -> fired[0] = true);

        slider.setValue(100);

        assertEquals(false, fired[0]);
    }

    @Test
    void theArrowKeysStepByOneAndThePageKeysByTen() {
        LevelSlider slider = new LevelSlider(0, 127, 64, Color.ORANGE, Color.GRAY);

        pressShortcut(slider, KeyStroke.getKeyStroke("RIGHT"));
        assertEquals(65, slider.getValue());

        pressShortcut(slider, KeyStroke.getKeyStroke("LEFT"));
        pressShortcut(slider, KeyStroke.getKeyStroke("LEFT"));
        assertEquals(63, slider.getValue());

        pressShortcut(slider, KeyStroke.getKeyStroke("PAGE_UP"));
        assertEquals(73, slider.getValue());

        pressShortcut(slider, KeyStroke.getKeyStroke("PAGE_DOWN"));
        assertEquals(63, slider.getValue());
    }

    @Test
    void theHomeAndEndKeysGoToTheExtremes() {
        LevelSlider slider = new LevelSlider(0, 127, 64, Color.ORANGE, Color.GRAY);

        pressShortcut(slider, KeyStroke.getKeyStroke("HOME"));
        assertEquals(0, slider.getValue());

        pressShortcut(slider, KeyStroke.getKeyStroke("END"));
        assertEquals(127, slider.getValue());
    }

    @Test
    void exposesItsCurrentValueToAssistiveTechnology() {
        LevelSlider slider = new LevelSlider(0, 127, 64, Color.ORANGE, Color.GRAY);

        assertEquals(AccessibleRole.SLIDER, slider.getAccessibleContext().getAccessibleRole());
        AccessibleValue accessibleValue = (AccessibleValue) slider.getAccessibleContext();
        assertEquals(64, accessibleValue.getCurrentAccessibleValue().intValue());
        assertEquals(0, accessibleValue.getMinimumAccessibleValue().intValue());
        assertEquals(127, accessibleValue.getMaximumAccessibleValue().intValue());
    }

    private static void pressShortcut(JComponent component, KeyStroke keyStroke) {
        Object name = component.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name).actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
    }
}
