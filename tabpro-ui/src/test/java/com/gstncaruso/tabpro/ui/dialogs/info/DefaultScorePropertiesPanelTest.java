package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import org.junit.jupiter.api.Test;

class DefaultScorePropertiesPanelTest {

    @Test
    void startsWithTheGivenDefaults() {
        NewScoreDefaults defaults = new NewScoreDefaults(
                90, new TimeSignature(3, 4), KeySignature.cMajor(), "Improvisando", "Yo");

        DefaultScorePropertiesPanel panel = new DefaultScorePropertiesPanel(defaults);

        assertEquals(defaults, panel.toDefaults());
    }

    @Test
    void reflectsWhateverYouLoadAfterwards() {
        DefaultScorePropertiesPanel panel = new DefaultScorePropertiesPanel(NewScoreDefaults.blank());

        panel.apply(new NewScoreDefaults(150, new TimeSignature(6, 8), KeySignature.cMajor(), "Otra", "Otro"));

        NewScoreDefaults result = panel.toDefaults();
        assertEquals(150, result.tempo());
        assertEquals(new TimeSignature(6, 8), result.timeSignature());
        assertEquals("Otra", result.title());
        assertEquals("Otro", result.artist());
    }
}
