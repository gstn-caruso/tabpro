package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class DefaultScorePropertiesPanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        NewScoreDefaults defaults = new NewScoreDefaults(
                120, new TimeSignature(4, 4), KeySignature.cMajor(), "", "");
        AccessibilityAssertions.assertNoViolations(new DefaultScorePropertiesPanel(defaults));
    }

    @Test
    void theTempoLabelIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Tempo", english.text("score_dialogs.DefaultScorePropertiesPanel.tempo"));
    }

    @Test
    void startsWithTheGivenDefaults() {
        NewScoreDefaults defaults = new NewScoreDefaults(
                90, new TimeSignature(3, 4), KeySignature.cMajor(), "Improvising", "Me");

        DefaultScorePropertiesPanel panel = new DefaultScorePropertiesPanel(defaults);

        assertEquals(defaults, panel.toDefaults());
    }

    @Test
    void reflectsWhateverYouLoadAfterwards() {
        DefaultScorePropertiesPanel panel = new DefaultScorePropertiesPanel(NewScoreDefaults.blank());

        panel.apply(new NewScoreDefaults(150, new TimeSignature(6, 8), KeySignature.cMajor(), "Another", "Someone"));

        NewScoreDefaults result = panel.toDefaults();
        assertEquals(150, result.tempo());
        assertEquals(new TimeSignature(6, 8), result.timeSignature());
        assertEquals("Another", result.title());
        assertEquals("Someone", result.artist());
    }
}
