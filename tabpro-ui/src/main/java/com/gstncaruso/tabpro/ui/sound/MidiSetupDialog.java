package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
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
import javax.swing.filechooser.FileNameExtensionFilter;

public final class MidiSetupDialog {

    private static final int PORT_COUNT = Ports.PORT_COUNT;
    private static final String GENERAL_MIDI_LABEL = "General MIDI";
    private static final String NO_SOUND_FONT_LABEL = "Sintetizador interno del JDK";

    private MidiSetupDialog() {
    }

    public record PortSetup(String device, String patchPath, boolean limitPitchVariation) {
    }

    public record Setup(
            String soundFontFile,
            boolean soundFontActive,
            List<PortSetup> ports,
            String input,
            int sensitivityMillis,
            StringAssignment strings) {

        public Setup {
            if (ports.size() != PORT_COUNT) {
                throw new IllegalArgumentException(PORT_COUNT + " ports are needed: " + ports.size());
            }
            ports = List.copyOf(ports);
        }
    }

    public static Optional<Setup> ask(Component parent, Ports.Devices devices, Setup current) {
        Fields fields = buildPanel(devices, current);

        boolean accepted = DialogShell.ask(parent, "Configuración MIDI", fields.panel());
        if (!accepted) {
            return Optional.empty();
        }
        List<PortSetup> ports = fields.rows().stream().map(PortRow::toSetup).toList();
        return Optional.of(new Setup(
                fields.soundFont().file(), fields.soundFont().active(), ports, selectionOf(fields.inputs()),
                (Integer) fields.sensitivity().getValue(), (StringAssignment) fields.assignment().getSelectedItem()));
    }

    static Fields buildPanel(Ports.Devices devices, Setup current) {
        FormPanel panel = new FormPanel();

        SoundFontRow soundFont = new SoundFontRow(current.soundFontFile(), current.soundFontActive());
        soundFont.addTo(panel);

        List<PortRow> rows = new ArrayList<>(PORT_COUNT);
        List<FormPanel.Section> portSections = new ArrayList<>(PORT_COUNT);
        for (int index = 0; index < PORT_COUNT; index++) {
            int port = index + 1;
            PortRow row = new PortRow(devices, port, current.ports().get(index));
            portSections.add(row.addTo(panel));
            rows.add(row);
        }
        panel.addSideBySide(portSections.get(0), portSections.get(1));
        panel.addSideBySide(portSections.get(2), portSections.get(3));

        panel.addSection("Entrada MIDI");
        JComboBox<String> inputs = comboOf(devices.inputs(), current.input());
        panel.addRow("Entrada de notas", inputs);
        JSpinner sensitivity = new JSpinner(new SpinnerNumberModel(current.sensitivityMillis(), 1, 2000, 5));
        panel.addRow("Sensibilidad (ms)", sensitivity);
        JComboBox<StringAssignment> assignment = new JComboBox<>(StringAssignment.values());
        assignment.setSelectedItem(current.strings());
        assignment.setRenderer(labelledBy(StringAssignment::label));
        panel.addRow("Cuerdas al capturar", assignment);

        return new Fields(panel, soundFont, rows, inputs, sensitivity, assignment);
    }

    record Fields(
            FormPanel panel,
            SoundFontRow soundFont,
            List<PortRow> rows,
            JComboBox<String> inputs,
            JSpinner sensitivity,
            JComboBox<StringAssignment> assignment) {
    }

    private static final class SoundFontRow {

        private final JLabel fileLabel = new JLabel();
        private final JCheckBox active;
        private final JButton loadButton;
        private final JButton clearButton;
        private String file;

        SoundFontRow(String currentFile, boolean currentlyActive) {
            file = currentFile;
            updateLabel();
            active = new JCheckBox("Banco activo (F2)", currentlyActive);

            loadButton = DialogStyle.flatButton("Cargar banco…");
            loadButton.addActionListener(event -> choose(loadButton));

            clearButton = DialogStyle.flatButton("Quitar");
            clearButton.addActionListener(event -> {
                file = "";
                updateLabel();
            });
        }

        void addTo(FormPanel panel) {
            panel.addSection("Banco de sonido (SoundFont)");
            JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, DialogStyle.GAP_S, 0));
            buttons.add(loadButton);
            buttons.add(clearButton);
            panel.addRow("Archivo", fileLabel, buttons);
            panel.addRow("", active);
        }

        String file() {
            return file;
        }

        boolean active() {
            return active.isSelected();
        }

        private void choose(Component parent) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Bancos SoundFont (.sf2, .dls)", "sf2", "dls"));
            if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            file = chooser.getSelectedFile().getAbsolutePath();
            active.setSelected(true);
            updateLabel();
        }

        private void updateLabel() {
            fileLabel.setText(file.isBlank() ? NO_SOUND_FONT_LABEL : Path.of(file).getFileName().toString());
        }
    }

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

        FormPanel.Section addTo(FormPanel panel) {
            FormPanel.Section section = panel.newDetachedSection("Puerto " + port);
            section.addRow("Dispositivo", device, testButton);
            JPanel patchButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, DialogStyle.GAP_S, 0));
            patchButtons.add(loadPatchButton);
            patchButtons.add(clearPatchButton);
            section.addRow("Patch de instrumentos", patchLabel, patchButtons);
            section.addRow("", limitPitchVariation);
            return section;
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
