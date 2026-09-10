package com.gstncaruso.tabpro.ui.testsupport;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;

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

    @SuppressWarnings("unchecked")
    public static String renderedTextOf(Container root, Class<?> itemType, Object item) {
        JComboBox combo = firstWithItemType(root, itemType);
        Component rendered =
                combo.getRenderer().getListCellRendererComponent(new JList<>(), item, 0, false, false);
        return ((JLabel) rendered).getText();
    }

    public static JList<?> firstListNamed(Container root, String accessibleName) {
        for (Component child : root.getComponents()) {
            if (child instanceof JList<?> list
                    && accessibleName.equals(list.getAccessibleContext().getAccessibleName())) {
                return list;
            }
            if (child instanceof Container container) {
                JList<?> found = firstListNamed(container, accessibleName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String renderedTextOfList(JList list, Object item) {
        Component rendered =
                list.getCellRenderer().getListCellRendererComponent(list, item, 0, false, false);
        return ((JLabel) rendered).getText();
    }

    public static JRadioButton radioButtonWithText(Container root, String text) {
        for (Component child : root.getComponents()) {
            if (child instanceof JRadioButton radio && text.equals(radio.getText())) {
                return radio;
            }
            if (child instanceof Container container) {
                JRadioButton found = radioButtonWithText(container, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
