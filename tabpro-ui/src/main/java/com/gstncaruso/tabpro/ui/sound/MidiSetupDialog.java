package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.ui.actions.Ports;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * La ventana de Options > MIDI Setup: por donde sale el sonido, por donde
 * entran las notas y como se reparten las cuerdas al capturar.
 */
public final class MidiSetupDialog {

    private MidiSetupDialog() {
    }

    /** Lo que la ventana devuelve: que dispositivos usar y como asignar las cuerdas. */
    public record Setup(String output, String input, StringAssignment strings) {
    }

    public static Optional<Setup> ask(Component parent, Ports.Devices devices, StringAssignment strings) {
        JComboBox<String> outputs = comboOf(devices.outputs(), devices.output());
        JComboBox<String> inputs = comboOf(devices.inputs(), devices.input());
        JComboBox<StringAssignment> assignment = new JComboBox<>(StringAssignment.values());
        assignment.setSelectedItem(strings);
        assignment.setRenderer(labelledBy(StringAssignment::label));

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Salida de sonido"));
        panel.add(outputs);
        panel.add(new JLabel("Entrada de notas"));
        panel.add(inputs);
        panel.add(new JLabel("Cuerdas al capturar"));
        panel.add(assignment);

        int answer = JOptionPane.showConfirmDialog(
                parent, panel, "Configuración MIDI", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        return Optional.of(new Setup(
                selectionOf(outputs), selectionOf(inputs), (StringAssignment) assignment.getSelectedItem()));
    }

    private static JComboBox<String> comboOf(List<String> options, String selected) {
        JComboBox<String> combo = new JComboBox<>(options.toArray(String[]::new));
        if (!selected.isBlank()) {
            combo.setSelectedItem(selected);
        }
        combo.setEnabled(!options.isEmpty());
        return combo;
    }

    private static String selectionOf(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    private static <T> javax.swing.ListCellRenderer<T> labelledBy(java.util.function.Function<T, String> label) {
        javax.swing.DefaultListCellRenderer renderer = new javax.swing.DefaultListCellRenderer() {

            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value != null) {
                    setText(label.apply((T) value));
                }
                return this;
            }
        };
        @SuppressWarnings("unchecked")
        javax.swing.ListCellRenderer<T> typed = (javax.swing.ListCellRenderer<T>) renderer;
        return typed;
    }
}
