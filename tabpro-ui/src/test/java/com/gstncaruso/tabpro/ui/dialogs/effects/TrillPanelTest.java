package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TrillPanelTest {

    @Test
    void theFretFieldIsAvailableInEnglish() {
        assertEquals("Fret of the second note",
                Texts.forLocale(Locale.ENGLISH).text("edit_dialogs.TrillPanel.fret"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new TrillPanel(Trill.to(0)));
    }

    @Test
    void theSpeedComboShowsTheNoteValueInSpanish() {
        TrillPanel panel = new TrillPanel(Trill.to(0));

        String renderedText = Combos.renderedTextOf(panel, NoteValue.class, NoteValue.QUARTER);

        assertEquals("Negra", renderedText);
    }

    @Test
    void startsWithTheGivenTrill() {
        Trill trill = new Trill(7, NoteValue.SIXTEENTH);

        TrillPanel panel = new TrillPanel(trill);

        assertEquals(trill, panel.toTrill());
    }
}
