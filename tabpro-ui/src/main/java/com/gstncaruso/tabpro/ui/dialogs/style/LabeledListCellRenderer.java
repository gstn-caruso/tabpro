package com.gstncaruso.tabpro.ui.dialogs.style;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * El renderer generico de todo combo o lista que muestra un tipo del dominio: pinta
 * {@link Labels#of(Object)} en vez de dejar que Swing caiga en el toString() del item.
 */
public final class LabeledListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null) {
            setText(Labels.of(value));
        }
        return this;
    }
}
