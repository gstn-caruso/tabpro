package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class NoteEffectsDialogTest {

    @Test
    void theTitleAndBendTabAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Note Effects", english.text("edit_dialogs.NoteEffectsDialog.title"));
        assertEquals("Bend", english.text("edit_dialogs.NoteEffectsDialog.bend"));
    }
}
