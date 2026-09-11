package com.gstncaruso.tabpro.ui.dialogs.instrument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.InstrumentPatch;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class InstrumentPanelTest {

    @Test
    void theSearchAndListFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Search Instrument", english.text("edit_dialogs.InstrumentPanel.search"));
        assertEquals("Instruments", english.text("edit_dialogs.InstrumentPanel.list"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new InstrumentPanel(0));
    }

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

    @Test
    void aPatchShowsItsOwnNamesInsteadOfTheGeneralMidiOnes() {
        InstrumentPatch patch = InstrumentPatch.parse("Requinto criollo");
        InstrumentPanel panel = new InstrumentPanel(0, patch);

        panel.search("Requinto");

        assertEquals(1, panel.visiblePrograms().size());
        assertTrue(panel.visiblePrograms().contains(0));
    }
}
