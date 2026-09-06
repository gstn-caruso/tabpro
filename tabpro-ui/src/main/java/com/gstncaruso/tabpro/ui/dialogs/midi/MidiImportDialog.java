package com.gstncaruso.tabpro.ui.dialogs.midi;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.track.AddTrackDialog;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * La ventana de "MIDI Import" del manual: a la izquierda las pistas del archivo elegido, con la
 * opcion de escuchar la o las que esten marcadas (tal como suenan en el MIDI, todavia sin
 * convertir) o de abrir otro archivo. El import rapido reemplaza la partitura entera con una
 * pista por cada pista elegida; el paso a paso no borra nada y deja traer el titulo y los
 * cambios de compas, agregar una pista nueva, y fusionar sobre la pista actual una o varias
 * pistas MIDI elegidas -- todo repetible.
 */
public final class MidiImportDialog {

    private MidiImportDialog() {
    }

    public static void show(
            Component parent,
            Editor editor,
            ScoreExchange exchange,
            BooleanSupplier askToDiscardChanges,
            Consumer<Score> adopt,
            Runnable afterChange,
            Path initialPath,
            Consumer<Timeline> onListen) {
        Path[] currentPath = {initialPath};
        MidiImportPanel panel;
        try {
            panel = new MidiImportPanel(exchange.midiTracksIn(currentPath[0]));
        } catch (ScoreFileException e) {
            showError(parent, e);
            return;
        }

        JButton openAnother = DialogStyle.flatButton("Abrir otro archivo…");
        JButton listen = DialogStyle.flatButton("Escuchar");
        JButton quickImport = DialogStyle.flatButton("Import rápido (reemplaza la partitura)");
        JButton titleAndTimeSignatures = DialogStyle.flatButton("Importar título y cambios de compás");
        JButton addTrack = DialogStyle.flatButton("Agregar una pista");
        JButton importOntoCurrent = DialogStyle.flatButton("Importar sobre la pista actual");

        JPanel top = flowOf(openAnother, listen);
        JPanel stepByStep = flowOf(titleAndTimeSignatures, addTrack, importOntoCurrent);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(flowOf(quickImport), BorderLayout.NORTH);
        bottom.add(stepByStep, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout());
        content.add(top, BorderLayout.NORTH);
        content.add(panel, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        openAnother.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(midiFilter());
            if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                currentPath[0] = chooser.getSelectedFile().toPath();
                panel.showTracks(exchange.midiTracksIn(currentPath[0]));
            } catch (ScoreFileException e) {
                showError(parent, e);
            }
        });

        listen.addActionListener(event -> withSelection(parent, panel, selected -> {
            try {
                onListen.accept(exchange.midiTrackTimeline(currentPath[0], selected));
            } catch (ScoreFileException e) {
                showError(parent, e);
            }
        }));

        quickImport.addActionListener(event -> withSelection(parent, panel, selected -> {
            if (!askToDiscardChanges.getAsBoolean()) {
                return;
            }
            try {
                Score imported = exchange.importMidiQuick(currentPath[0], selected, panel.transposeDownOneOctave());
                adopt.accept(imported);
                afterChange.run();
            } catch (ScoreFileException e) {
                showError(parent, e);
            }
        }));

        titleAndTimeSignatures.addActionListener(event -> {
            try {
                editor.apply(score -> exchange.importMidiTitleAndTimeSignatures(score, currentPath[0]));
                afterChange.run();
            } catch (ScoreFileException e) {
                showError(parent, e);
            }
        });

        addTrack.addActionListener(event -> AddTrackDialog.show(parent, editor));

        importOntoCurrent.addActionListener(event -> withSelection(parent, panel, selected -> {
            try {
                int trackIndex = editor.cursor().track();
                boolean transpose = panel.transposeDownOneOctave();
                editor.apply(score -> score.mappingTrack(
                        trackIndex, track -> exchange.importMidiInto(track, currentPath[0], selected, transpose)));
                afterChange.run();
            } catch (ScoreFileException e) {
                showError(parent, e);
            }
        }));

        DialogShell.show(parent, "Importar MIDI", content);
    }

    private static void withSelection(Component parent, MidiImportPanel panel, Consumer<List<Integer>> action) {
        List<Integer> selected = panel.selectedTrackIndices();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent, "Elegí al menos una pista de la lista para importar.", "tabpro", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        action.accept(selected);
    }

    private static JPanel flowOf(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        for (JButton button : buttons) {
            row.add(button);
        }
        return row;
    }

    private static FileNameExtensionFilter midiFilter() {
        return new FileNameExtensionFilter("Archivos MIDI (*.mid)", "mid", "midi");
    }

    private static void showError(Component parent, ScoreFileException e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "tabpro", JOptionPane.ERROR_MESSAGE);
    }
}
