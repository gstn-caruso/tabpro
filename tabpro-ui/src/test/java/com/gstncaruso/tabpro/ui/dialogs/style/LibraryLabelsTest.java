package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class LibraryLabelsTest {

    private static final Texts ENGLISH = Texts.forLocale(Locale.ENGLISH);

    @Test
    void anEditedTuningIsLabeledAsCustomInSpanish() {
        assertEquals("Personalizada (DADGBE)", Labels.of(Tuning.standard().withStringPitch(6, new Pitch(38))));
    }

    @Test
    void aCustomTuningIsNamedCustomInEnglish() {
        assertEquals("Custom", ENGLISH.text("library.tuning.custom"));
    }

    @Test
    void thePercussionKitTuningIsLabeledPercusionInSpanishAndNamedPercussionInEnglish() {
        assertEquals("Percusión (CCCCCC)", Labels.of(PercussionKit.tuning()));
        assertEquals("Percussion", ENGLISH.text("library.tuning.percussion"));
    }

    @Test
    void aTuningNamedByTheUserIsLabeledWithThatNameAsTyped() {
        assertEquals("Mi afinación (DADGAD)", Labels.of(Tuning.of("Mi afinación", 62, 57, 55, 50, 45, 38)));
    }
}
