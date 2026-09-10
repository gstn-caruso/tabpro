package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
    void elComboDeEscalaMuestraElNombreEnCastellanoEnVezDelRecordCrudo() {
        Editor editor = new Editor(Score.blank());

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), new ChosenScale());

        @SuppressWarnings("unchecked")
        JComboBox<Scale> scales = Combos.firstWithItemType(panel, Scale.class);
        Component rendered = scales.getRenderer()
                .getListCellRendererComponent(new JList<>(), ScaleLibrary.major(), 0, false, false);

        assertEquals("Mayor (Jonico)", ((JLabel) rendered).getText());
    }
}
