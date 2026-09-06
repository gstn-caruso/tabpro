package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.awt.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * La ventana de Options > MIDI Setup: los cuatro puertos de salida con su
 * patch de instrumentos y su Limit Pitch Variation, la entrada de notas con
 * su sensibilidad, y como se reparten las cuerdas al capturar.
 */
public final class MidiSetupDialog {

    private static final int PORT_COUNT = Ports.PORT_COUNT;
    private static final String GENERAL_MIDI_LABEL = "General MIDI";

    private MidiSetupDialog() {
    }

    /** Un puerto de salida: su dispositivo, el patch que muestra sus nombres y si limita la variacion de altura. */
    public record PortSetup(String device, String patchPath, boolean limitPitchVariation) {
    }

    /** Lo que la ventana devuelve: los cuatro puertos, la entrada y como asignar las cuerdas. */
    public record Setup(List<PortSetup> ports, String input, int sensitivityMillis, StringAssignment strings) {

        public Setup {
            if (ports.size() != PORT_COUNT) {
                throw new IllegalArgumentException("hacen falta " + PORT_COUNT + " puertos: " + ports.size());
            }
            ports = List.copyOf(ports);
        }
    }

    public static Optional<Setup> ask(Component parent, Ports.Devices devices, Setup current) {
        FormPanel panel = new FormPanel();
        List<PortRow> rows = new ArrayList<>(PORT_COUNT);
        for (int index = 0; index < PORT_COUNT; index++) {
            int port = index + 1;
            PortRow row = new PortRow(devices, port, current.ports().get(index));
            row.addTo(panel);
            rows.add(row);
        }

        panel.addSection("Entrada MIDI");
        JComboBox<String> inputs = comboOf(devices.inputs(), current.input());
        panel.addRow("Entrada de notas", inputs);
        JSpinner sensitivity = new JSpinner(new SpinnerNumberModel(current.sensitivityMillis(), 1, 2000, 5));
        panel.addRow("Sensibilidad (ms)", sensitivity);
        JComboBox<StringAssignment> assignment = new JComboBox<>(StringAssignment.values());
        assignment.setSelectedItem(current.strings());
        assignment.setRenderer(labelledBy(StringAssignment::label));
        panel.addRow("Cuerdas al capturar", assignment);

        int answer = JOptionPane.showConfirmDialog(
                parent, panel, "Configuración MIDI", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        List<PortSetup> ports = rows.stream().map(PortRow::toSetup).toList();
        return Optional.of(new Setup(
                ports, selectionOf(inputs), (Integer) sensitivity.getValue(),
                (StringAssignment) assignment.getSelectedItem()));
    }

    /** Los controles de un puerto: dispositivo con su prueba, patch de instrumentos y Limit Pitch Variation. */
    private static final class PortRow {

        private final int port;
        private final JComboBox<String> device;
        private final JCheckBox limitPitchVariation;
        private final JLabel patchLabel = new JLabel();
        private final JButton testButton;
        private final JButton loadPatchButton;
        private final JButton clearPatchButton;
        private String patchPath;

        PortRow(Ports.Devices devices, int port, PortSetup current) {
            this.port = port;
            device = comboOf(devices.outputs(), current.device());
            limitPitchVariation = new JCheckBox("Limit Pitch Variation", current.limitPitchVariation());
            patchPath = current.patchPath();
            updatePatchLabel();

            testButton = DialogStyle.flatButton("Probar");
            testButton.addActionListener(event -> devices.playTestNote(selectionOf(device)));

            loadPatchButton = DialogStyle.flatButton("Cargar patch…");
            loadPatchButton.addActionListener(event -> choosein(loadPatchButton));

            clearPatchButton = DialogStyle.flatButton("Quitar");
            clearPatchButton.addActionListener(event -> {
                patchPath = "";
                updatePatchLabel();
            });
        }

        void addTo(FormPanel panel) {
            panel.addSection("Puerto " + port);
            panel.addRow("Dispositivo", device, testButton);
            JPanel patchButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, DialogStyle.GAP_S, 0));
            patchButtons.add(loadPatchButton);
            patchButtons.add(clearPatchButton);
            panel.addRow("Patch de instrumentos", patchLabel, patchButtons);
            panel.addRow("", limitPitchVariation);
        }

        PortSetup toSetup() {
            return new PortSetup(selectionOf(device), patchPath, limitPitchVariation.isSelected());
        }

        private void choosein(Component parent) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            Path chosen = chooser.getSelectedFile().toPath();
            if (!isReadable(chosen)) {
                JOptionPane.showMessageDialog(
                        parent, "No se pudo leer el patch elegido.", "Configuración MIDI", JOptionPane.ERROR_MESSAGE);
                return;
            }
            patchPath = chosen.toString();
            updatePatchLabel();
        }

        private static boolean isReadable(Path path) {
            try {
                Files.readString(path);
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        private void updatePatchLabel() {
            patchLabel.setText(patchPath.isBlank() ? GENERAL_MIDI_LABEL : Path.of(patchPath).getFileName().toString());
        }
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
