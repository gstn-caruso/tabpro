package com.gstncaruso.tabpro.ui.dialogs.markers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class MarkerPanelTest {

    @Test
    void theNameAndColorFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Name", english.text("edit_dialogs.MarkerPanel.name"));
        assertEquals("Color", english.text("edit_dialogs.shared.color"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new MarkerPanel(new Marker("Solo", ScoreColor.rgb(0))));
    }

    @Test
    void startsWithTheGivenMarker() {
        Marker marker = new Marker("Solo", ScoreColor.rgb(0x00FF00));

        MarkerPanel panel = new MarkerPanel(marker);

        assertEquals(marker, panel.toMarker());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        MarkerPanel panel = new MarkerPanel(Marker.named("A"));

        panel.apply(new Marker("B", ScoreColor.rgb(0x0000FF)));

        assertEquals(new Marker("B", ScoreColor.rgb(0x0000FF)), panel.toMarker());
    }
}
