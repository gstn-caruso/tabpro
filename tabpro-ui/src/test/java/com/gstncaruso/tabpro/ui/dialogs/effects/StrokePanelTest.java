package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import org.junit.jupiter.api.Test;

class StrokePanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new StrokePanel(Stroke.of(StrokeDirection.DOWN)));
    }

    @Test
    void elComboDeVelocidadMuestraLaFiguraEnCastellano() {
        StrokePanel panel = new StrokePanel(Stroke.of(StrokeDirection.DOWN));

        String texto = Combos.renderedTextOf(panel, NoteValue.class, NoteValue.QUARTER);

        assertEquals("Negra", texto);
    }

    @Test
    void startsWithTheGivenStroke() {
        Stroke stroke = new Stroke(StrokeDirection.UP, NoteValue.EIGHTH, true);

        StrokePanel panel = new StrokePanel(stroke);

        assertEquals(stroke, panel.toStroke());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        StrokePanel panel = new StrokePanel(Stroke.of(StrokeDirection.DOWN));

        panel.apply(new Stroke(StrokeDirection.UP, NoteValue.SIXTEENTH, false));

        assertEquals(new Stroke(StrokeDirection.UP, NoteValue.SIXTEENTH, false), panel.toStroke());
    }
}
