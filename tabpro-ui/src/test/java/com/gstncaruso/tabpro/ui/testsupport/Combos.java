package com.gstncaruso.tabpro.ui.testsupport;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComboBox;

/**
 * Encuentra un combo dentro de un panel de prueba por el tipo de sus items, sin obligar
 * al panel a exponer un campo privado solo para poder testearlo.
 */
public final class Combos {

    private Combos() {
    }

    @SuppressWarnings("rawtypes")
    public static JComboBox firstWithItemType(Container root, Class<?> itemType) {
        for (Component child : root.getComponents()) {
            if (child instanceof JComboBox combo
                    && combo.getItemCount() > 0
                    && itemType.isInstance(combo.getItemAt(0))) {
                return combo;
            }
            if (child instanceof Container container) {
                JComboBox found = firstWithItemType(container, itemType);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
