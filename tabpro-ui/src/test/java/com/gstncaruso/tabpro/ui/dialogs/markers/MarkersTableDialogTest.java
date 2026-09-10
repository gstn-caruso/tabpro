package com.gstncaruso.tabpro.ui.dialogs.markers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import org.junit.jupiter.api.Test;

class MarkersTableDialogTest {

    @Test
    void emptyScoreLeavesOnlyAddEnabled() {
        Editor editor = new Editor(Score.blank());

        JPanel content = MarkersTableDialog.buildContent(editor, () -> {
        });

        JTable table = tableOf(content);
        assertEquals(0, table.getRowCount());
        assertTrue(buttonLabeled(content, "Agregar").isEnabled());
        assertFalse(buttonLabeled(content, "Editar").isEnabled());
        assertFalse(buttonLabeled(content, "Borrar").isEnabled());
        assertFalse(buttonLabeled(content, "Ir a").isEnabled());
    }

    @Test
    void selectingTheOnlyRowEnablesEditDeleteAndGoTo() {
        Editor editor = new Editor(Score.blank());
        editor.setMarker(Marker.named("Intro"));

        JPanel content = MarkersTableDialog.buildContent(editor, () -> {
        });
        JTable table = tableOf(content);
        table.setRowSelectionInterval(0, 0);

        assertTrue(buttonLabeled(content, "Editar").isEnabled());
        assertTrue(buttonLabeled(content, "Borrar").isEnabled());
        assertTrue(buttonLabeled(content, "Ir a").isEnabled());
    }

    private JTable tableOf(Container root) {
        for (Component component : root.getComponents()) {
            if (component instanceof JTable table) {
                return table;
            }
            if (component instanceof Container container) {
                JTable found = tableOf(container);
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
