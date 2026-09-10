package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
