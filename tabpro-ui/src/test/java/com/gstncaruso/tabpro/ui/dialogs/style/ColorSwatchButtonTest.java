package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ColorSwatchButtonTest {

    @Test
    void theAccessibleNameAndPickerTitleAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Color", english.text("edit_dialogs.shared.color"));
        assertEquals("Choose Color", english.text("edit_dialogs.ColorSwatchButton.pick"));
    }
}
