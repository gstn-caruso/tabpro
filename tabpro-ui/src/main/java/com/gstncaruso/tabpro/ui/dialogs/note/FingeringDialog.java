package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.awt.Component;
import java.util.Optional;
import javax.swing.JComboBox;

/** La ventana de digitacion: que dedo pisa la cuerda y que dedo la toca. */
public final class FingeringDialog {

    private static final String NONE = "Sin indicar";

    private FingeringDialog() {
    }

    public static void show(Component parent, Editor editor) {
        Optional<Finger> left = editor.currentNote().flatMap(note -> note.effects().leftHand());
        Optional<Finger> right = editor.currentNote().flatMap(note -> note.effects().rightHand());

        JComboBox<Object> leftHand = fingers(left, Finger::leftHandSymbol);
        JComboBox<Object> rightHand = fingers(right, Finger::rightHandSymbol);

        FormPanel form = new FormPanel()
                .addRow("Mano izquierda", leftHand)
                .addRow("Mano derecha", rightHand);

        if (!DialogShell.ask(parent, "Digitación", form)) {
            return;
        }
        editor.setLeftHandFinger(chosen(leftHand));
        editor.setRightHandFinger(chosen(rightHand));
    }

    private static JComboBox<Object> fingers(
            Optional<Finger> selected, java.util.function.Function<Finger, String> symbol) {
        JComboBox<Object> combo = new JComboBox<>();
        combo.addItem(NONE);
        for (Finger finger : Finger.values()) {
            combo.addItem(finger);
        }
        combo.setSelectedItem(selected.map(Object.class::cast).orElse(NONE));
        combo.setRenderer(new javax.swing.DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, isSelected, focused);
                if (value instanceof Finger finger) {
                    setText(symbol.apply(finger) + " — " + finger.name().toLowerCase(java.util.Locale.ROOT));
                }
                return this;
            }
        });
        return combo;
    }

    private static Finger chosen(JComboBox<Object> combo) {
        Object selected = combo.getSelectedItem();
        return selected instanceof Finger finger ? finger : null;
    }
}
