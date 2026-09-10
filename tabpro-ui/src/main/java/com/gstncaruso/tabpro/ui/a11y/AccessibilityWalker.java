package com.gstncaruso.tabpro.ui.a11y;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

public final class AccessibilityWalker {

    /** "labeledBy" is the client property that JLabel#setLabelFor leaves on the labeled component. */
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
        if (hidesLookAndFeelChildren(component)) {
            return;
        }
        for (Component child : childrenOf(component)) {
            visit(child, path + " > " + child.getClass().getSimpleName(), violations);
        }
    }

    /** A JMenu's items live in its JPopupMenu, not among its regular AWT children. */
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
        violations.addAll(rawDomainTextViolations(component, path));
    }

    private List<Violation> rawDomainTextViolations(Component component, String path) {
        if (component instanceof JComboBox<?> combo) {
            return rawDomainTextViolations(path, combo.getModel(), combo.getRenderer());
        }
        if (component instanceof JList<?> list) {
            return rawDomainTextViolations(path, list.getModel(), list.getCellRenderer());
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<Violation> rawDomainTextViolations(
            String path, ListModel<?> model, ListCellRenderer<?> renderer) {
        List<Violation> violations = new ArrayList<>();
        ListCellRenderer<Object> typedRenderer = (ListCellRenderer<Object>) renderer;
        JList<Object> rendererContext = new JList<>();
        for (int index = 0; index < model.getSize(); index++) {
            Object item = model.getElementAt(index);
            if (!isDomainValue(item)) {
                continue;
            }
            Component rendered =
                    typedRenderer.getListCellRendererComponent(rendererContext, item, index, false, false);
            String renderedText = rendered instanceof JLabel label ? label.getText() : null;
            if (isNotBlank(renderedText) && matchesRawToString(item, renderedText)) {
                violations.add(new Violation(path, "toString() crudo: " + renderedText));
            }
        }
        return violations;
    }

    private boolean isDomainValue(Object item) {
        return item != null && (item instanceof Enum<?> || item.getClass().isRecord());
    }

    private boolean matchesRawToString(Object item, String text) {
        if (item instanceof Enum<?> enumValue) {
            return text.equals(enumValue.name());
        }
        return text.equals(syntheticRecordToString(item));
    }

    private String syntheticRecordToString(Object item) {
        RecordComponent[] components = item.getClass().getRecordComponents();
        StringBuilder raw = new StringBuilder(item.getClass().getSimpleName()).append('[');
        for (int index = 0; index < components.length; index++) {
            if (index > 0) {
                raw.append(", ");
            }
            raw.append(components[index].getName()).append('=').append(rawComponentValue(components[index], item));
        }
        return raw.append(']').toString();
    }

    private Object rawComponentValue(RecordComponent component, Object item) {
        try {
            return component.getAccessor().invoke(item);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo leer " + component.getName() + " de " + item, e);
        }
    }

    private boolean isInteractive(Component component) {
        return isStandardAtomicControl(component)
                || component instanceof JTabbedPane
                || component instanceof AccessibleControl;
    }

    private boolean hidesLookAndFeelChildren(Component component) {
        return isStandardAtomicControl(component) || component instanceof JScrollBar;
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
        if (isLabeledByVisibleText(component)) {
            return true;
        }
        if (component instanceof AbstractButton button) {
            return isNotBlank(button.getText());
        }
        if (component instanceof JLabel label) {
            return isNotBlank(label.getText());
        }
        return component instanceof JTabbedPane pane && hasAnyTabTitle(pane);
    }

    private boolean hasAnyTabTitle(JTabbedPane pane) {
        for (int tab = 0; tab < pane.getTabCount(); tab++) {
            if (isNotBlank(pane.getTitleAt(tab))) {
                return true;
            }
        }
        return false;
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
