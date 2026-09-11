package com.gstncaruso.tabpro.ui.dialogs.pagesetup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class PageSetupDialogTest {

    @Test
    void theTitleAndRefreshButtonAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Page Setup", english.text("score_dialogs.PageSetupDialog.title"));
        assertEquals("Refresh Score", english.text("score_dialogs.PageSetupDialog.refreshScore"));
    }
}
