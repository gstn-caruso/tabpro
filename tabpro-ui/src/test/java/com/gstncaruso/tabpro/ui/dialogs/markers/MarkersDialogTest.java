package com.gstncaruso.tabpro.ui.dialogs.markers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class MarkersDialogTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        Editor editor = new Editor(Score.blank());

        AccessibilityAssertions.assertNoViolations(MarkersDialog.buildContent(editor));
    }

    @Test
    void arrancaConElFormularioCargadoConElMarcadorDelCompasPedido() {
        Editor editor = new Editor(Score.blank());
        editor.setMarker(Marker.named("Intro"));

        JPanel content = MarkersDialog.buildContentEditing(editor, 0);

        assertEquals("Intro", formOf(content).toMarker().name());
    }

    @Test
    void guardarCambiosActualizaElMarcadorDelCompasPedidoAunqueElCursorEsteEnOtro() {
        Editor editor = new Editor(scoreWithMeasures(3));
        editor.moveTo(0, 0, 1);
        editor.setMarker(Marker.named("Intro"));
        editor.moveTo(2, 0, 1);

        JPanel content = MarkersDialog.buildContentEditing(editor, 0);
        formOf(content).apply(new Marker("Estribillo", Marker.DEFAULT_COLOR));
        buttonLabeled(content, "Guardar cambios").doClick();

        assertEquals("Estribillo", editor.score().attributesOf(0).marker().get().name());
    }

    private Score scoreWithMeasures(int count) {
        Score score = Score.blank();
        for (int i = 1; i < count; i++) {
            score = score.withMeasureInsertedInEveryTrackAt(i);
        }
        return score;
    }

    private MarkerPanel formOf(Container root) {
        for (Component component : root.getComponents()) {
            if (component instanceof MarkerPanel form) {
                return form;
            }
            if (component instanceof Container container) {
                MarkerPanel found = formOf(container);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private JButton buttonLabeled(Container root, String text) {
        for (Component component : root.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container container) {
                JButton found = buttonLabeled(container, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
