package com.gstncaruso.tabpro.ui.dialogs.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TripletFeelPanelTest {

    @Test
    void theTripletFeelFieldIsAvailableInEnglish() {
        assertEquals("Triplet feel",
                Texts.forLocale(Locale.ENGLISH).text("edit_dialogs.MeasurePropertiesDialog.tripletFeel"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new TripletFeelPanel(TripletFeel.EIGHTH));
    }

    @Test
    void startsWithTheGivenFeel() {
        TripletFeelPanel panel = new TripletFeelPanel(TripletFeel.EIGHTH);

        assertEquals(TripletFeel.EIGHTH, panel.toTripletFeel());
    }

    @Test
    void canBeSetBackToNone() {
        TripletFeelPanel panel = new TripletFeelPanel(TripletFeel.SIXTEENTH);

        panel.apply(TripletFeel.NONE);

        assertEquals(TripletFeel.NONE, panel.toTripletFeel());
    }
}
