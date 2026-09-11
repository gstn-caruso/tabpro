package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.i18n.Texts;
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
        DialogShell.show(parent, Texts.get("edit_dialogs.MarkersTableDialog.title"), closer -> buildContent(editor, closer));
    }

    static JPanel buildContent(Editor editor, Runnable onClose) {
        MarkerTableModel model = new MarkerTableModel(editor.score());
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getAccessibleContext().setAccessibleName(Texts.get("edit_dialogs.MarkersDialog.title"));
        table.setToolTipText(Texts.get("edit_dialogs.MarkersDialog.title"));

        JButton add = DialogStyle.flatButton(Texts.get("edit_dialogs.MarkersTableDialog.add"));
        JButton edit = DialogStyle.flatButton(Texts.get("edit_dialogs.MarkersTableDialog.edit"));
        JButton delete = DialogStyle.flatButton(Texts.get("edit_dialogs.MarkersTableDialog.delete"));
        JButton goTo = DialogStyle.flatButton(Texts.get("edit_dialogs.MarkersTableDialog.goTo"));
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
            Marker defaultMarker = Marker.named(Texts.get("edit_dialogs.MarkersDialog.defaultName"));
            if (editMarker(table, editor, defaultMarker, editor.cursor().measure(),
                    Texts.get("edit_dialogs.MarkersTableDialog.addTitle"))) {
                model.refresh(editor.score());
            }
        });

        edit.addActionListener(event -> {
            MarkerList.Positioned positioned = model.rowAt(table.getSelectedRow());
            if (editMarker(table, editor, positioned.marker(), positioned.measureIndex(),
                    Texts.get("edit_dialogs.MarkersTableDialog.editTitle"))) {
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
                JOptionPane.showMessageDialog(parent, Texts.get("edit_dialogs.MarkersDialog.nameRequired"));
            }
        }
        return false;
    }
}
