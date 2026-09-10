package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public final class MarkersTableDialog {

    private MarkersTableDialog() {
    }

    public static void show(Component parent, Editor editor) {
        DialogShell.show(parent, "Lista de marcadores", closer -> buildContent(editor, closer));
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

        delete.addActionListener(event -> {
            MarkerList.Positioned positioned = model.rowAt(table.getSelectedRow());
            editor.moveTo(positioned.measureIndex(), 0, 1);
            editor.setMarker(null);
            model.refresh(editor.score());
        });

        goTo.addActionListener(event -> {
            MarkerList.Positioned positioned = model.rowAt(table.getSelectedRow());
            editor.moveTo(positioned.measureIndex(), 0, 1);
            onClose.run();
        });

        add.addActionListener(event -> {
            if (editMarker(table, editor, Marker.named("Marcador"), editor.cursor().measure(), "Agregar un marcador")) {
                model.refresh(editor.score());
            }
        });

        edit.addActionListener(event -> {
            MarkerList.Positioned positioned = model.rowAt(table.getSelectedRow());
            if (editMarker(table, editor, positioned.marker(), positioned.measureIndex(), "Editar el marcador")) {
                model.refresh(editor.score());
            }
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

    private static boolean editMarker(Component parent, Editor editor, Marker initial, int measureIndex, String title) {
        MarkerPanel form = new MarkerPanel(initial);
        while (DialogShell.ask(parent, title, form)) {
            try {
                Marker marker = form.toMarker();
                editor.moveTo(measureIndex, 0, 1);
                editor.setMarker(marker);
                return true;
            } catch (IllegalArgumentException invalidName) {
                JOptionPane.showMessageDialog(parent, "El marcador necesita un nombre.");
            }
        }
        return false;
    }
}
