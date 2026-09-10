package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
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
}
