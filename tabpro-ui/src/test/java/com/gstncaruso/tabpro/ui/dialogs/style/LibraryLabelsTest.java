package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import org.junit.jupiter.api.Test;

class LibraryLabelsTest {

    @Test
    void anEditedTuningIsLabeledAsCustomInSpanish() {
        assertEquals("Personalizada (DADGBE)", Labels.of(Tuning.standard().withStringPitch(6, new Pitch(38))));
    }

    @Test
    void aTuningNamedByTheUserIsLabeledWithThatNameAsTyped() {
        assertEquals("Mi afinación (DADGAD)", Labels.of(Tuning.of("Mi afinación", 62, 57, 55, 50, 45, 38)));
    }
}
