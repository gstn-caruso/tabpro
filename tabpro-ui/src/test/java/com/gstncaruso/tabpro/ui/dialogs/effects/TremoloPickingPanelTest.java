package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import org.junit.jupiter.api.Test;

class TremoloPickingPanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new TremoloPickingPanel(TremoloPicking.at(NoteValue.SIXTEENTH)));
    }

    @Test
    void theSpeedComboShowsTheNoteValueInSpanish() {
        TremoloPickingPanel panel = new TremoloPickingPanel(TremoloPicking.at(NoteValue.SIXTEENTH));

        String renderedText = Combos.renderedTextOf(panel, NoteValue.class, NoteValue.QUARTER);

        assertEquals("Negra", renderedText);
    }

    @Test
    void startsWithTheGivenSpeed() {
        TremoloPicking picking = TremoloPicking.at(NoteValue.THIRTY_SECOND);

        TremoloPickingPanel panel = new TremoloPickingPanel(picking);

        assertEquals(picking, panel.toTremoloPicking());
    }
}
