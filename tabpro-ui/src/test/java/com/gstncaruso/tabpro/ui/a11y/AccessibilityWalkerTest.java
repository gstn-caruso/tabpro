package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import org.junit.jupiter.api.Test;

class AccessibilityWalkerTest {

    private final AccessibilityWalker walker = new AccessibilityWalker();

    @Test
    void anEmptyPanelHasNoViolations() {
        assertTrue(walker.walk(new JPanel()).isEmpty());
    }

    @Test
    void aButtonWithNoNameOrTextIsANameViolation() {
        JPanel panel = new JPanel();
        JButton button = new JButton();
        panel.add(button);

        List<Violation> violations = walker.walk(panel);

        assertEquals(1, violations.stream().filter(v -> v.reason().equals("sin nombre accesible")).count());
    }

    @Test
    void aButtonWithVisibleTextBringsItsOwnAccessibleName() {
        JPanel panel = new JPanel();
        JButton button = new JButton("Guardar");
        panel.add(button);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void anIconOnlyButtonWithNameAndTooltipHasNoViolations() {
        JPanel panel = new JPanel();
        JButton button = new JButton();
        button.setText(null);
        button.getAccessibleContext().setAccessibleName("Deshacer");
        button.setToolTipText("Deshacer  [Ctrl+Z]");
        panel.add(button);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void anIconOnlyButtonWithNameButNoTooltipIsATooltipViolation() {
        JPanel panel = new JPanel();
        JButton button = new JButton();
        button.setText(null);
        button.getAccessibleContext().setAccessibleName("Deshacer");
        panel.add(button);

        List<Violation> violations = walker.walk(panel);

        assertEquals(1, violations.size());
        assertEquals("sin tooltip y sin texto visible", violations.get(0).reason());
    }

    @Test
    void aBlankAccessibleNameCountsAsNoName() {
        JPanel panel = new JPanel();
        JButton button = new JButton();
        button.getAccessibleContext().setAccessibleName("   ");
        panel.add(button);

        List<Violation> violations = walker.walk(panel);

        assertTrue(violations.stream().anyMatch(v -> v.reason().equals("sin nombre accesible")));
    }

    @Test
    void theViolationPathIncludesEveryIntermediateContainer() {
        JPanel root = new JPanel();
        JPanel row = new JPanel();
        JButton button = new JButton();
        row.add(button);
        root.add(row);

        List<Violation> violations = walker.walk(root);

        assertEquals("JPanel > JPanel > JButton", violations.get(0).path());
    }

    @Test
    void severalUnnamedControlsProduceSeveralViolations() {
        JPanel panel = new JPanel();
        panel.add(new JButton());
        panel.add(new JButton());
        panel.add(new JButton("Con nombre"));

        List<Violation> violations = walker.walk(panel);

        assertEquals(2, violations.stream().filter(v -> v.reason().equals("sin nombre accesible")).count());
    }

    @Test
    void aLabelLinkedWithSetLabelForCountsAsVisibleTextForTheCombo() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Tono:");
        JComboBox<String> combo = new JComboBox<>(new String[] {"Do"});
        label.setLabelFor(combo);
        panel.add(label);
        panel.add(combo);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void aLabelLinkedWithSetLabelForCountsAsVisibleTextForATextlessButton() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Color");
        JButton button = new JButton();
        button.setText(null);
        label.setLabelFor(button);
        panel.add(label);
        panel.add(button);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void aComboWithNoLinkedLabelIsANameAndTooltipViolation() {
        JPanel panel = new JPanel();
        panel.add(new JComboBox<String>(new String[] {"Do"}));

        List<Violation> violations = walker.walk(panel);

        assertEquals(2, violations.size());
    }

    @Test
    void aCustomComponentMarkedWithoutANameProducesANameAndTooltipViolation() {
        class Knob extends JPanel implements AccessibleControl {}
        JPanel panel = new JPanel();
        panel.add(new Knob());

        List<Violation> violations = walker.walk(panel);

        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.reason().equals("sin nombre accesible")));
        assertTrue(violations.stream().anyMatch(v -> v.reason().equals("sin tooltip y sin texto visible")));
    }

    @Test
    void aCustomComponentMarkedWithNameAndTooltipHasNoViolations() {
        class Knob extends JPanel implements AccessibleControl {}
        Knob knob = new Knob();
        knob.getAccessibleContext().setAccessibleName("Volumen de Guitarra");
        knob.setToolTipText("Volumen de Guitarra");
        JPanel panel = new JPanel();
        panel.add(knob);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void aMenuItemWithNoTextInsideAJMenuIsAViolation() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Archivo");
        menu.add(new JMenuItem());
        bar.add(menu);

        List<Violation> violations = walker.walk(bar);

        assertTrue(violations.stream().anyMatch(v -> v.reason().equals("sin nombre accesible")));
    }

    @Test
    void aMenuItemWithTextInsideAJMenuHasNoViolations() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Archivo");
        menu.add(new JMenuItem("Nuevo"));
        bar.add(menu);

        assertTrue(walker.walk(bar).isEmpty());
    }

    @Test
    void theArrowButtonsOfAScrollPaneAreNotApplicationControls() {
        JPanel panel = new JPanel();
        panel.add(new JScrollPane(new JTextArea()));

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void aJTabbedPaneWithTabTitlesDoesNotNeedATooltip() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bend", new JPanel());
        tabs.addTab("Armonicos", new JPanel());

        assertTrue(walker.walk(tabs).isEmpty());
    }

    @Test
    void aPlainJPanelIsNotAnInteractiveControl() {
        JPanel panel = new JPanel();
        panel.add(new JPanel());

        assertTrue(walker.walk(panel).isEmpty());
    }

    private enum NoteDuration { QUARTER, EIGHTH }

    private record Scale(String name) {
    }

    @Test
    void aComboWithTheDefaultRendererShowingTheRawNameOfAnEnumIsAViolation() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Figura");
        JComboBox<NoteDuration> combo = new JComboBox<>(new NoteDuration[] {NoteDuration.QUARTER});
        label.setLabelFor(combo);
        panel.add(label);
        panel.add(combo);

        List<Violation> violations = walker.walk(panel);

        assertEquals(1, violations.size());
        assertEquals("toString() crudo: QUARTER", violations.get(0).reason());
    }

    @Test
    void aComboWithTheDefaultRendererShowingTheRawToStringOfARecordIsAViolation() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Escala");
        JComboBox<Scale> combo = new JComboBox<>(new Scale[] {new Scale("Mayor")});
        label.setLabelFor(combo);
        panel.add(label);
        panel.add(combo);

        List<Violation> violations = walker.walk(panel);

        assertEquals(1, violations.size());
        assertTrue(violations.get(0).reason().startsWith("toString() crudo: Scale["));
    }

    private record PaperFormat(String label, int width, int height) {

        @Override
        public String toString() {
            return label;
        }
    }

    @Test
    void aComboShowingTheCustomToStringOfARecordIsNotAViolation() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Formato");
        JComboBox<PaperFormat> combo =
                new JComboBox<>(new PaperFormat[] {new PaperFormat("A4", 210, 297)});
        label.setLabelFor(combo);
        panel.add(label);
        panel.add(combo);

        assertTrue(walker.walk(panel).isEmpty());
    }

    private enum Dynamic {
        FORTE;

        @Override
        public String toString() {
            return "f";
        }
    }

    @Test
    void aComboShowingTheCustomToStringOfAnEnumIsNotAViolation() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Dinamica");
        JComboBox<Dynamic> combo = new JComboBox<>(new Dynamic[] {Dynamic.FORTE});
        label.setLabelFor(combo);
        panel.add(label);
        panel.add(combo);

        assertTrue(walker.walk(panel).isEmpty());
    }

    @Test
    void aComboWithACustomRendererShowingTheRawNameOfAnEnumIsAViolation() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Figura");
        JComboBox<NoteDuration> combo = new JComboBox<>(new NoteDuration[] {NoteDuration.QUARTER});
        label.setLabelFor(combo);
        combo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                setText(String.valueOf(value));
                return this;
            }
        });
        panel.add(label);
        panel.add(combo);

        List<Violation> violations = walker.walk(panel);

        assertEquals(1, violations.size());
        assertEquals("toString() crudo: QUARTER", violations.get(0).reason());
    }

    @Test
    void aListWithTheDefaultRendererShowingTheRawNameOfAnEnumIsAViolation() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Figura");
        JList<NoteDuration> list = new JList<>(new NoteDuration[] {NoteDuration.QUARTER});
        label.setLabelFor(list);
        panel.add(label);
        panel.add(list);

        List<Violation> violations = walker.walk(panel);

        assertEquals(1, violations.size());
        assertEquals("toString() crudo: QUARTER", violations.get(0).reason());
    }
}
