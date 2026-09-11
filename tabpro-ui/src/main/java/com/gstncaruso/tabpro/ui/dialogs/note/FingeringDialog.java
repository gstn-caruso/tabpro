package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import java.util.Optional;
import javax.swing.JComboBox;
import javax.swing.JComponent;

public final class FingeringDialog {

    private static final String NONE = Texts.get("edit_dialogs.FingeringDialog.none");

    private FingeringDialog() {
    }

    public static void show(Component parent, Editor editor) {
        open(parent, editor, Hand.LEFT);
    }

    public static void showFocusedOnRightHand(Component parent, Editor editor) {
        open(parent, editor, Hand.RIGHT);
    }

    private static void open(Component parent, Editor editor, Hand initialFocus) {
        Optional<Finger> left = editor.currentNote().flatMap(note -> note.effects().leftHand());
        Optional<Finger> right = editor.currentNote().flatMap(note -> note.effects().rightHand());
        Fields fields = buildFields(left, right, initialFocus);

        if (!DialogShell.ask(parent, Texts.get("edit_dialogs.FingeringDialog.title"), fields.form(), fields.initialFocus())) {
            return;
        }
        editor.setLeftHandFinger(chosen(fields.leftHand()));
        editor.setRightHandFinger(chosen(fields.rightHand()));
    }

    enum Hand { LEFT, RIGHT }

    static Fields buildFields(Optional<Finger> left, Optional<Finger> right) {
        return buildFields(left, right, Hand.LEFT);
    }

    static Fields buildFields(Optional<Finger> left, Optional<Finger> right, Hand initialFocus) {
        JComboBox<Object> leftHand = fingers(left, Finger::leftHandSymbol);
        JComboBox<Object> rightHand = fingers(right, Finger::rightHandSymbol);

        FormPanel form = new FormPanel()
                .addRow(Texts.get("edit_dialogs.FingeringDialog.leftHand"), leftHand)
                .addRow(Texts.get("edit_dialogs.FingeringDialog.rightHand"), rightHand);

        return new Fields(form, leftHand, rightHand, initialFocus == Hand.RIGHT ? rightHand : leftHand);
    }

    record Fields(FormPanel form, JComboBox<Object> leftHand, JComboBox<Object> rightHand, JComponent initialFocus) {
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
