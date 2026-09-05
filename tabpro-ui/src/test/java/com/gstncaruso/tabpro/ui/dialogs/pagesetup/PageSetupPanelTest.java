package com.gstncaruso.tabpro.ui.dialogs.pagesetup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PageSetupPanelTest {

    @Test
    void startsWithTheGivenSetup() {
        PageSetup setup = new PageSetup(PaperFormat.LETTER, Orientation.LANDSCAPE, 10, 15, 20, 25, 80, "[%title]", "[%page]/[%pages]");

        PageSetupPanel panel = new PageSetupPanel(setup);

        assertEquals(setup, panel.toPageSetup());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        PageSetupPanel panel = new PageSetupPanel(PageSetup.defaults());

        panel.apply(new PageSetup(PaperFormat.A3, Orientation.LANDSCAPE, 5, 5, 5, 5, 50, "H", "F"));

        assertEquals(new PageSetup(PaperFormat.A3, Orientation.LANDSCAPE, 5, 5, 5, 5, 50, "H", "F"), panel.toPageSetup());
    }
}
