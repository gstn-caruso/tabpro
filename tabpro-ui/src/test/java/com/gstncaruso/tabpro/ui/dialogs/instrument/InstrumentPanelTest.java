package com.gstncaruso.tabpro.ui.dialogs.instrument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InstrumentPanelTest {

    @Test
    void startsWithTheTracksProgramSelected() {
        InstrumentPanel panel = new InstrumentPanel(105);

        assertEquals(105, panel.selectedProgram());
    }

    @Test
    void searchingNarrowsTheVisiblePrograms() {
        InstrumentPanel panel = new InstrumentPanel(0);

        panel.search("banjo");

        assertEquals(1, panel.visiblePrograms().size());
        assertTrue(panel.visiblePrograms().contains(105));
    }

    @Test
    void searchingSelectsTheFirstMatchWhenTheCurrentOneDisappears() {
        InstrumentPanel panel = new InstrumentPanel(0);

        panel.search("banjo");

        assertEquals(105, panel.selectedProgram());
    }

    @Test
    void pickingAProgramDirectlyWorksWithoutSearching() {
        InstrumentPanel panel = new InstrumentPanel(0);

        panel.selectProgram(41);

        assertEquals(41, panel.selectedProgram());
    }
}
