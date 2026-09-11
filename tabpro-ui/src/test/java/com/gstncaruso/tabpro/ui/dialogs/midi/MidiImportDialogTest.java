package com.gstncaruso.tabpro.ui.dialogs.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class MidiImportDialogTest {

    @Test
    void theTitleAndFileFilterAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("MIDI Import", english.text("score_dialogs.MidiImportDialog.title"));
        assertEquals("MIDI Files (*.mid)", english.text("score_dialogs.MidiImportDialog.fileFilter"));
    }
}
