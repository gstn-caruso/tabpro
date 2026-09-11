package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TogglableEffectPanelTest {

    @Test
    void theActiveCheckboxIsAvailableInEnglish() {
        assertEquals("Active", Texts.forLocale(Locale.ENGLISH).text("edit_dialogs.TogglableEffectPanel.active"));
    }
}
