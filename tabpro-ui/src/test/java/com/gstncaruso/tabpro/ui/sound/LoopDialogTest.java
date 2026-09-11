package com.gstncaruso.tabpro.ui.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class LoopDialogTest {

    @Test
    void theFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Loop / Speed Trainer", english.text("views.LoopDialog.title"));
        assertEquals("Simple Loop", english.text("views.LoopDialog.simpleLoop"));
        assertEquals("Speed Trainer", english.text("views.LoopDialog.speedTrainer"));
        assertEquals("From Bar", english.text("views.LoopDialog.fromBar"));
        assertEquals("To Bar", english.text("views.LoopDialog.toBar"));
        assertEquals("Initial Tempo", english.text("views.LoopDialog.initialTempo"));
        assertEquals("Final Tempo", english.text("views.LoopDialog.finalTempo"));
        assertEquals("Increase per Loop", english.text("views.LoopDialog.increasePerLoop"));
    }
}
