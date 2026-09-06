package com.gstncaruso.tabpro.ui.dialogs.pagesetup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.ui.page.Orientation;
import com.gstncaruso.tabpro.ui.page.PageBanner;
import com.gstncaruso.tabpro.ui.page.PageElement;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.page.PaperFormat;
import org.junit.jupiter.api.Test;

class PageSetupPanelTest {

    @Test
    void startsWithTheGivenSetup() {
        PageSetup setup = new PageSetup(
                PaperFormat.LETTER, Orientation.LANDSCAPE, 10, 15, 20, 25, 80,
                PageBanner.header(), PageBanner.footer());

        PageSetupPanel panel = new PageSetupPanel(setup);

        assertEquals(setup, panel.toPageSetup());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        PageSetupPanel panel = new PageSetupPanel(PageSetup.defaults());
        PageSetup another = new PageSetup(
                PaperFormat.A3, Orientation.LANDSCAPE, 5, 5, 5, 5, 50,
                PageBanner.header(), PageBanner.footer());

        panel.apply(another);

        assertEquals(another, panel.toPageSetup());
    }

    @Test
    void keepsTheTickAndTheTextOfEveryElementOfTheHeaderAndTheFooter() {
        PageSetup setup = new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, 20, 20, 20, 20, 100,
                PageBanner.header().with(PageElement.TITLE, true, "[%title] ([%artist])")
                        .with(PageElement.ALBUM, false, "[%album]"),
                PageBanner.footer().with(PageElement.PAGE_NUMBER, true, "[%page]/[%pages]"));

        PageSetupPanel panel = new PageSetupPanel(setup);
        PageSetup readBack = panel.toPageSetup();

        assertEquals("[%title] ([%artist])", readBack.header().textOf(PageElement.TITLE));
        assertFalse(readBack.header().shows(PageElement.ALBUM));
        assertEquals("[%page]/[%pages]", readBack.footer().textOf(PageElement.PAGE_NUMBER));
    }
}
