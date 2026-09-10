package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import org.junit.jupiter.api.Test;

class TrillPanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new TrillPanel(Trill.to(0)));
    }

    @Test
    void elComboDeVelocidadMuestraLaFiguraEnCastellano() {
        TrillPanel panel = new TrillPanel(Trill.to(0));

        String texto = Combos.renderedTextOf(panel, NoteValue.class, NoteValue.QUARTER);

        assertEquals("Negra", texto);
    }

    @Test
    void startsWithTheGivenTrill() {
        Trill trill = new Trill(7, NoteValue.SIXTEENTH);

        TrillPanel panel = new TrillPanel(trill);

        assertEquals(trill, panel.toTrill());
    }
}
