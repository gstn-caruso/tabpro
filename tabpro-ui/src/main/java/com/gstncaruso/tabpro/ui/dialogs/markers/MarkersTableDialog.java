package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 * La lista de marcadores como tabla (Posicion/Nombre) con Agregar/Editar/Borrar/Ir a al costado:
 * la vista de gestion de Guitar Pro 5, separada del editor de un marcador ({@link MarkerPanel}).
 */
public final class MarkersTableDialog {

    private MarkersTableDialog() {
    }

    static JPanel buildContent(Editor editor, Runnable onClose) {
        MarkerTableModel model = new MarkerTableModel(editor.score());
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getAccessibleContext().setAccessibleName("Marcadores");
        table.setToolTipText("Marcadores");

        JButton add = DialogStyle.flatButton("Agregar");
        JButton edit = DialogStyle.flatButton("Editar");
        JButton delete = DialogStyle.flatButton("Borrar");
        JButton goTo = DialogStyle.flatButton("Ir a");
        edit.setEnabled(false);
        delete.setEnabled(false);
        goTo.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(event -> {
            boolean hasSelection = table.getSelectedRow() >= 0;
            edit.setEnabled(hasSelection);
            delete.setEnabled(hasSelection);
            goTo.setEnabled(hasSelection);
        });

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(add);
        buttons.add(edit);
        buttons.add(delete);
        buttons.add(goTo);

        JPanel content = new JPanel(new BorderLayout(DialogStyle.GAP_S, 0));
        DialogStyle.padded(content);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(buttons, BorderLayout.EAST);
        return content;
    }
}
