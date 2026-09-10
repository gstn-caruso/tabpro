package com.gstncaruso.tabpro.ui.a11y;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * Recorre un arbol de componentes Swing y devuelve los controles interactivos que no tienen
 * nombre accesible, o que no tienen tooltip cuando tampoco muestran texto propio.
 */
public final class AccessibilityWalker {

    /** "labeledBy" es la client property que JLabel#setLabelFor deja en el componente etiquetado. */
    private static final String LABELED_BY_PROPERTY = "labeledBy";

    public List<Violation> walk(Container root) {
        List<Violation> violations = new ArrayList<>();
        visit(root, root.getClass().getSimpleName(), violations);
        return violations;
    }

    private void visit(Component component, String path, List<Violation> violations) {
        if (isInteractive(component)) {
            check(component, path, violations);
        }
        if (isStandardAtomicControl(component)) {
            return;
        }
        for (Component child : childrenOf(component)) {
            visit(child, path + " > " + child.getClass().getSimpleName(), violations);
        }
    }

    /** Los items de un JMenu viven en su JPopupMenu, no entre sus hijos AWT normales. */
    private Component[] childrenOf(Component component) {
        if (component instanceof JMenu menu) {
            return menu.getMenuComponents();
        }
        if (component instanceof Container container) {
            return container.getComponents();
        }
        return new Component[0];
    }

    private void check(Component component, String path, List<Violation> violations) {
        if (!hasAccessibleName(component)) {
            violations.add(new Violation(path, "sin nombre accesible"));
        }
        if (!hasTooltip(component) && !hasVisibleText(component)) {
            violations.add(new Violation(path, "sin tooltip y sin texto visible"));
        }
    }

    private boolean isInteractive(Component component) {
        return isStandardAtomicControl(component)
                || component instanceof JTabbedPane
                || component instanceof AccessibleControl;
    }

    private boolean isStandardAtomicControl(Component component) {
        return (component instanceof AbstractButton && !(component instanceof JMenu))
                || component instanceof JComboBox
                || component instanceof JSpinner
                || component instanceof JTextField
                || component instanceof JList
                || component instanceof JSlider;
    }

    private boolean hasAccessibleName(Component component) {
        return isNotBlank(component.getAccessibleContext().getAccessibleName());
    }

    private boolean hasTooltip(Component component) {
        return component instanceof JComponent jComponent && isNotBlank(jComponent.getToolTipText());
    }

    private boolean hasVisibleText(Component component) {
        if (component instanceof AbstractButton button) {
            return isNotBlank(button.getText());
        }
        if (component instanceof JLabel label) {
            return isNotBlank(label.getText());
        }
        return isLabeledByVisibleText(component);
    }

    private boolean isLabeledByVisibleText(Component component) {
        if (!(component instanceof JComponent jComponent)) {
            return false;
        }
        Object labeledBy = jComponent.getClientProperty(LABELED_BY_PROPERTY);
        return labeledBy instanceof JLabel label && isNotBlank(label.getText());
    }

    private boolean isNotBlank(String text) {
        return text != null && !text.isBlank();
    }
}
