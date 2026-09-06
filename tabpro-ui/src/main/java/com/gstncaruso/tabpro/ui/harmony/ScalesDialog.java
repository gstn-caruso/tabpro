package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleFinder;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import com.gstncaruso.tabpro.core.harmony.ScaleMatch;
import com.gstncaruso.tabpro.core.harmony.ScaleTone;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * La ventana de escalas: ver y escuchar cualquier escala en cualquier tonalidad,
 * y buscar cual usa un rango de compases de la partitura.
 */
public final class ScalesDialog {

    /** La octava en la que se escuchan las notas de la escala. */
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
        private final JComboBox<PitchClass> tonics = new JComboBox<>();
        private final JComboBox<Scale> scales = new JComboBox<>(ScaleLibrary.all().toArray(Scale[]::new));
        private final DefaultListModel<ScaleTone> tones = new DefaultListModel<>();
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
            PitchClasses.chromatic().forEach(tonics::addItem);
            chosen.tonic().ifPresent(tonics::setSelectedItem);
            chosen.scale().ifPresent(scales::setSelectedItem);

            setLayout(new BorderLayout(10, 10));
            add(chooserZone(), BorderLayout.NORTH);
            add(tonesZone(), BorderLayout.CENTER);
            add(finderZone(), BorderLayout.SOUTH);
            tonics.addActionListener(event -> chooseScale());
            scales.addActionListener(event -> chooseScale());
            chooseScale();
        }

        private JPanel chooserZone() {
            JPanel zone = new JPanel(new GridLayout(1, 0, 8, 0));
            zone.add(labelled("Tonalidad", tonics));
            zone.add(labelled("Escala", scales));
            return zone;
        }

        private JScrollPane tonesZone() {
            JList<ScaleTone> list = new JList<>(tones);
            list.setCellRenderer(new javax.swing.DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(
                        JList<?> owner, Object value, int index, boolean selected, boolean focused) {
                    super.getListCellRendererComponent(owner, value, index, selected, focused);
                    if (value instanceof ScaleTone tone) {
                        setText(describe(tone));
                    }
                    return this;
                }
            });
            list.addListSelectionListener(event -> {
                ScaleTone tone = list.getSelectedValue();
                if (tone != null) {
                    player.playNote(
                            new Pitch(LISTENING_OCTAVE + tone.pitchClass().semitone()),
                            editor.currentTrack().channel().program());
                }
            });
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(BorderFactory.createTitledBorder("Notas de la escala"));
            scroll.setPreferredSize(new Dimension(420, 190));
            return scroll;
        }

        private static String describe(ScaleTone tone) {
            return tone.pitchClass().name()
                    + "   ·   grado " + tone.degree()
                    + "   ·   " + tone.interval().label();
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
            list.setCellRenderer(new javax.swing.DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(
                        JList<?> owner, Object value, int index, boolean selected, boolean focused) {
                    super.getListCellRendererComponent(owner, value, index, selected, focused);
                    if (value instanceof ScaleMatch match) {
                        setText(match.tonic().name() + " " + match.scale().name()
                                + "   [" + match.incidentNotes() + "]");
                    }
                    return this;
                }
            });
            list.addListSelectionListener(event -> {
                ScaleMatch match = list.getSelectedValue();
                if (match != null) {
                    tonics.setSelectedItem(match.tonic());
                    scales.setSelectedItem(match.scale());
                }
            });
            JScrollPane scroll = new JScrollPane(list);
            scroll.setPreferredSize(new Dimension(420, 120));
            zone.add(scroll, BorderLayout.CENTER);
            return zone;
        }

        private void chooseScale() {
            PitchClass tonic = (PitchClass) tonics.getSelectedItem();
            Scale scale = (Scale) scales.getSelectedItem();
            if (tonic == null || scale == null) {
                return;
            }
            chosen.choose(tonic, scale);
            tones.clear();
            chosen.tones().forEach(tones::addElement);
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
            row.add(new JLabel(label), BorderLayout.WEST);
            row.add(field, BorderLayout.CENTER);
            return row;
        }
    }
}
