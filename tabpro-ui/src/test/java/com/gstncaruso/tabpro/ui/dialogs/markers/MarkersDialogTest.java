package com.gstncaruso.tabpro.ui.dialogs.markers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Component;
import java.awt.Container;
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
}
