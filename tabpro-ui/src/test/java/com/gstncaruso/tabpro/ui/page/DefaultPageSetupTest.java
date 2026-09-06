package com.gstncaruso.tabpro.ui.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DefaultPageSetupTest {

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName());
    private final DefaultPageSetup stored = new DefaultPageSetup(scratch);

    @AfterEach
    void clearsTheScratchNode() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void withoutAnythingSavedTheDefaultIsTheOneTabproShipsWith() {
        assertEquals(PageSetup.defaults(), stored.get());
    }

    @Test
    void savingASetupMakesItTheDefault() {
        PageSetup mine = new PageSetup(
                PaperFormat.LEGAL, Orientation.LANDSCAPE, 5, 10, 15, 20, 75,
                PageBanner.header(), PageBanner.footer());

        stored.save(mine);

        assertEquals(mine, stored.get());
    }

    @Test
    void savingAgainReplacesWhatWasThere() {
        stored.save(new PageSetup(
                PaperFormat.A3, Orientation.PORTRAIT, 1, 1, 1, 1, 50, PageBanner.header(), PageBanner.footer()));
        PageSetup last = new PageSetup(
                PaperFormat.LETTER, Orientation.PORTRAIT, 30, 30, 30, 30, 120,
                PageBanner.header(), PageBanner.footer());

        stored.save(last);

        assertEquals(last, stored.get());
    }

    @Test
    void theTicksAndTheTextsOfTheHeaderAndTheFooterSurvive() {
        PageSetup mine = new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, 20, 20, 20, 20, 100,
                PageBanner.header()
                        .with(PageElement.TITLE, true, "[%title] en vivo")
                        .with(PageElement.ALBUM, false, "[%album]"),
                PageBanner.footer().with(PageElement.PAGE_NUMBER, true, "[%page]/[%pages]"));

        stored.save(mine);

        assertEquals(mine, stored.get());
    }

    @Test
    void aSetupSavedInGibberishGoesBackToTheOneTabproShipsWith() {
        stored.save(PageSetup.defaults());
        scratch.put("paperFormat", "papel de calcar");

        assertEquals(PageSetup.defaults(), stored.get());
    }
}
