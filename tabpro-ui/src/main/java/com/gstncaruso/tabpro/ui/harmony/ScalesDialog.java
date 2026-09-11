package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleFinder;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import com.gstncaruso.tabpro.core.harmony.ScaleMatch;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import com.gstncaruso.tabpro.ui.icons.Icons;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class ScalesDialog {

    private static final int LISTENING_OCTAVE = 60;

    private ScalesDialog() {
    }

    public static void show(Component parent, Editor editor, Player player, ChosenScale chosen) {
        Panel panel = new Panel(editor, player, chosen);
        DialogShell.show(parent, "Escalas", panel);
    }

    static final class Panel extends JPanel {

        private final Editor editor;
        private final Player player;
        private final ChosenScale chosen;
        private final DefaultListModel<PitchClass> tonicsModel = new DefaultListModel<>();
        private final JList<PitchClass> tonics = new JList<>(tonicsModel);
        private final DefaultListModel<Scale> scalesModel = new DefaultListModel<>();
        private final JList<Scale> scales = new JList<>(scalesModel);
        private final ScaleDegreesView degrees = new ScaleDegreesView();
        private final DefaultListModel<ScaleMatch> matches = new DefaultListModel<>();
        private final JSpinner fromMeasure;
        private final JSpinner toMeasure;

        Panel(Editor editor, Player player, ChosenScale chosen) {
            this.editor = editor;
            this.player = player;
            this.chosen = chosen;
            int lastMeasure = editor.currentTrack().measureCount();
            fromMeasure = new JSpinner(new SpinnerNumberModel(1, 1, lastMeasure, 1));
            toMeasure = new JSpinner(new SpinnerNumberModel(lastMeasure, 1, lastMeasure, 1));
            tonics.setCellRenderer(new LabeledListCellRenderer());
            tonics.getAccessibleContext().setAccessibleName("Tonalidad");
            tonics.setToolTipText("Tonalidad");
            scales.setCellRenderer(new LabeledListCellRenderer());
            scales.getAccessibleContext().setAccessibleName("Escala");
            scales.setToolTipText("Escala");
            PitchClasses.chromatic().forEach(tonicsModel::addElement);
            ScaleLibrary.all().forEach(scalesModel::addElement);
            tonics.setSelectedValue(chosen.tonic().orElseGet(() -> PitchClass.of("C")), true);
            scales.setSelectedValue(chosen.scale().orElseGet(ScaleLibrary::major), true);

            setLayout(new BorderLayout(10, 10));
            add(chooserZone(), BorderLayout.NORTH);
            add(degreesZone(), BorderLayout.CENTER);
            add(finderZone(), BorderLayout.SOUTH);
            tonics.addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    chooseScale();
                }
            });
            scales.addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    chooseScale();
                }
            });
            chooseScale();
        }

        private JPanel chooserZone() {
            JPanel zone = new JPanel(new GridLayout(1, 0, 8, 0));
            zone.add(labelled("Tonalidad", new JScrollPane(tonics)));
            zone.add(labelled("Escala", new JScrollPane(scales)));
            return zone;
        }

        private JPanel degreesZone() {
            JPanel zone = new JPanel(new BorderLayout());
            zone.setBorder(BorderFactory.createTitledBorder("Grados de la escala"));
            zone.add(listenBar(), BorderLayout.NORTH);
            zone.add(degrees, BorderLayout.CENTER);
            return zone;
        }

        private JPanel listenBar() {
            JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton listen = new JButton(Icons.play());
            listen.setToolTipText("Escuchar la escala");
            listen.getAccessibleContext().setAccessibleName("Escuchar");
            listen.addActionListener(event -> listenToScale());
            bar.add(listen);
            return bar;
        }

        private void listenToScale() {
            List<Pitch> pitches = chosen.tones().stream()
                    .map(tone -> new Pitch(LISTENING_OCTAVE + tone.pitchClass().semitone()))
                    .toList();
            player.playSequence(pitches, editor.currentTrack().channel().program());
        }

        private JPanel finderZone() {
            JPanel zone = new JPanel(new BorderLayout(8, 8));
            zone.setBorder(BorderFactory.createTitledBorder("Buscar la escala de la partitura"));
            JPanel range = new JPanel(new GridLayout(1, 0, 8, 0));
            range.add(labelled("Desde el compás", fromMeasure));
            range.add(labelled("Hasta el compás", toMeasure));
            JButton find = new JButton("Buscar");
            find.addActionListener(event -> findScales());
            range.add(find);
            zone.add(range, BorderLayout.NORTH);

            JList<ScaleMatch> list = new JList<>(matches);
            list.getAccessibleContext().setAccessibleName("Escalas encontradas");
            list.setToolTipText("Escalas encontradas");
            list.setCellRenderer(new javax.swing.DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(
                        JList<?> owner, Object value, int index, boolean selected, boolean focused) {
                    super.getListCellRendererComponent(owner, value, index, selected, focused);
                    if (value instanceof ScaleMatch match) {
                        setText(match.tonic().name() + " " + Labels.of(match.scale())
                                + "   [" + match.incidentNotes() + "]");
                    }
                    return this;
                }
            });
            list.addListSelectionListener(event -> {
                ScaleMatch match = list.getSelectedValue();
                if (match != null) {
                    tonics.setSelectedValue(match.tonic(), true);
                    scales.setSelectedValue(match.scale(), true);
                }
            });
            JScrollPane scroll = new JScrollPane(list);
            scroll.setPreferredSize(new Dimension(420, 120));
            zone.add(scroll, BorderLayout.CENTER);
            return zone;
        }

        private void chooseScale() {
            PitchClass tonic = tonics.getSelectedValue();
            Scale scale = scales.getSelectedValue();
            if (tonic == null || scale == null) {
                return;
            }
            chosen.choose(tonic, scale);
            degrees.show(chosen.tones());
        }

        private void findScales() {
            List<ScaleMatch> found = ScaleFinder.findIn(
                    editor.currentTrack(), value(fromMeasure) - 1, value(toMeasure) - 1);
            matches.clear();
            found.forEach(matches::addElement);
        }

        private static int value(JSpinner spinner) {
            return (Integer) spinner.getValue();
        }

        private static JPanel labelled(String label, Component field) {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            JLabel text = new JLabel(label);
            if (field instanceof javax.swing.JComponent labeledField) {
                text.setLabelFor(labeledField);
            }
            row.add(text, BorderLayout.WEST);
            row.add(field, BorderLayout.CENTER);
            return row;
        }
    }
}
