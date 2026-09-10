package com.gstncaruso.tabpro.ui.dialogs.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class TimeSignaturePanelTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        AccessibilityAssertions.assertNoViolations(new TimeSignaturePanel(new TimeSignature(4, 4)));
    }

    @Test
    void startsWithTheGivenSignature() {
        TimeSignaturePanel panel = new TimeSignaturePanel(new TimeSignature(3, 4));

        assertEquals(new TimeSignature(3, 4), panel.toTimeSignature());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        TimeSignaturePanel panel = new TimeSignaturePanel(TimeSignature.fourFour());

        panel.apply(new TimeSignature(6, 8));

        assertEquals(new TimeSignature(6, 8), panel.toTimeSignature());
    }
}
