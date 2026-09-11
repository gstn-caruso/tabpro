package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class HarmonicPanelTest {

    @Test
    void theTypeFieldIsAvailableInEnglish() {
        assertEquals("Type", Texts.forLocale(Locale.ENGLISH).text("edit_dialogs.shared.type"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new HarmonicPanel(HarmonicType.NATURAL));
    }

    @Test
    void startsWithTheGivenType() {
        for (HarmonicType type : HarmonicType.values()) {
            HarmonicPanel panel = new HarmonicPanel(type);
            assertEquals(type, panel.toHarmonicType());
        }
    }
}
