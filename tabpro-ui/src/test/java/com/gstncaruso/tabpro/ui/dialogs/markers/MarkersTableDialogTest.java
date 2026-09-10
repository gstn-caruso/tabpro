package com.gstncaruso.tabpro.ui.dialogs.markers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import org.junit.jupiter.api.Test;

class MarkersTableDialogTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        Editor editor = new Editor(Score.blank());
        editor.setMarker(Marker.named("Intro"));

        AccessibilityAssertions.assertNoViolations(MarkersTableDialog.buildContent(editor, () -> {
        }));
    }

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

    @Test
    void deletingTheSelectedRowRemovesTheMarkerFromTheModel() {
        Editor editor = new Editor(Score.blank());
        editor.setMarker(Marker.named("Intro"));

        JPanel content = MarkersTableDialog.buildContent(editor, () -> {
        });
        tableOf(content).setRowSelectionInterval(0, 0);
        buttonLabeled(content, "Borrar").doClick();

        assertTrue(editor.score().attributesOf(0).marker().isEmpty());
    }

    @Test
    void goingToTheSelectedRowMovesTheCursorAndCloses() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.moveToLastMeasure();
        editor.setMarker(Marker.named("Estribillo"));
        editor.moveToFirstMeasure();
        boolean[] closed = {false};

        JPanel content = MarkersTableDialog.buildContent(editor, () -> closed[0] = true);
        tableOf(content).setRowSelectionInterval(0, 0);
        buttonLabeled(content, "Ir a").doClick();

        assertEquals(1, editor.cursor().measure());
        assertTrue(closed[0]);
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
