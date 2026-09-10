package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class ScalesDialogTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        Editor editor = new Editor(Score.blank());

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), new ChosenScale());

        AccessibilityAssertions.assertNoViolations(panel);
    }

    @Test
    void laListaDeTonalidadMuestraElNombreDeLaNotaEnCastellano() {
        Editor editor = new Editor(Score.blank());

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), new ChosenScale());

        JList<?> tonics = Combos.firstListNamed(panel, "Tonalidad");

        assertEquals("C (Do)", Combos.renderedTextOfList(tonics, PitchClass.of("C")));
    }

    @Test
    void laListaDeEscalaMuestraElNombreEnCastellanoEnVezDelRecordCrudo() {
        Editor editor = new Editor(Score.blank());

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), new ChosenScale());

        JList<?> scales = Combos.firstListNamed(panel, "Escala");

        assertEquals("Mayor (Jonico)", Combos.renderedTextOfList(scales, ScaleLibrary.major()));
    }

    @Test
    void alElegirTonicaYEscalaEnLasListasLaEleccionLlegaAChosenScale() {
        Editor editor = new Editor(Score.blank());
        ChosenScale chosen = new ChosenScale();

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), chosen);
        JList<PitchClass> tonics = (JList<PitchClass>) Combos.firstListNamed(panel, "Tonalidad");
        JList<com.gstncaruso.tabpro.core.harmony.Scale> scales =
                (JList<com.gstncaruso.tabpro.core.harmony.Scale>) Combos.firstListNamed(panel, "Escala");

        tonics.setSelectedValue(PitchClass.of("D"), true);
        scales.setSelectedValue(ScaleLibrary.major(), true);

        assertTrue(chosen.tonic().isPresent());
        assertEquals(PitchClass.of("D"), chosen.tonic().orElseThrow());
        assertEquals(ScaleLibrary.major(), chosen.scale().orElseThrow());
    }
}
