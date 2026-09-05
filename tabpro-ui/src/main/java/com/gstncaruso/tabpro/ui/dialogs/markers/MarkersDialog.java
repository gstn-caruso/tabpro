package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * La ventana de Marcadores: insertar uno en el compas donde esta el cursor, y la
 * lista de los que ya hay para navegar hasta ellos o editarlos.
 */
public final class MarkersDialog {

    private MarkersDialog() {
    }

    public static void show(Component parent, Editor editor) {
        MarkerPanel form = new MarkerPanel(Marker.named("Marcador"));
        DefaultListModel<MarkerList.Positioned> model = new DefaultListModel<>();
        JList<MarkerList.Positioned> list = new JList<>(model);
        list.setCellRenderer((jlist, value, index, isSelected, hasFocus) -> new javax.swing.JLabel(value.label()));
        refresh(model, editor);

        JButton insert = DialogStyle.flatButton("Insertar aqui");
        JButton goTo = DialogStyle.flatButton("Ir al compas");
        JButton save = DialogStyle.flatButton("Guardar cambios");

        insert.addActionListener(event -> withValidName(form, name -> {
            editor.setMarker(form.toMarker());
            refresh(model, editor);
        }));
        goTo.addActionListener(event -> selected(list).ifPresent(positioned ->
                editor.moveTo(positioned.measureIndex(), 0, 1)));
        save.addActionListener(event -> selected(list).ifPresent(positioned -> withValidName(form, name -> {
            editor.moveTo(positioned.measureIndex(), 0, 1);
            editor.setMarker(form.toMarker());
            refresh(model, editor);
        })));
        list.addListSelectionListener(event -> selected(list).ifPresent(positioned -> form.apply(positioned.marker())));

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, DialogStyle.GAP_S, 0));
        buttons.add(insert);
        buttons.add(goTo);
        buttons.add(save);

        JPanel content = new JPanel(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(content);
        content.add(form, BorderLayout.NORTH);
        content.add(new JScrollPane(list), BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        DialogShell.show(parent, "Marcadores", content);
    }

    private static void refresh(DefaultListModel<MarkerList.Positioned> model, Editor editor) {
        model.clear();
        for (MarkerList.Positioned positioned : MarkerList.collect(editor.score())) {
            model.addElement(positioned);
        }
    }

    private static java.util.Optional<MarkerList.Positioned> selected(JList<MarkerList.Positioned> list) {
        return java.util.Optional.ofNullable(list.getSelectedValue());
    }

    private static void withValidName(MarkerPanel form, java.util.function.Consumer<String> action) {
        try {
            action.accept(form.toMarker().name());
        } catch (IllegalArgumentException invalidName) {
            JOptionPane.showMessageDialog(form, "El marcador necesita un nombre.");
        }
    }
}
