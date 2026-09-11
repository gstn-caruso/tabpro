package com.gstncaruso.tabpro.ui.dialogs.midi;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.ErrorTexts;
import com.gstncaruso.tabpro.ui.dialogs.track.AddTrackDialog;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

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
            Player player) {
        Path[] currentPath = {initialPath};
        MidiImportPanel panel;
        try {
            panel = new MidiImportPanel(
                    exchange.midiTracksIn(currentPath[0]), player,
                    selected -> timelineOrSilence(parent, exchange, currentPath[0], selected));
        } catch (ScoreFileException e) {
            showError(parent, e);
            return;
        }

        JButton openAnother = DialogStyle.flatButton(Texts.get("score_dialogs.MidiImportDialog.openAnotherFile"));
        JButton quickImport = DialogStyle.flatButton(Texts.get("score_dialogs.MidiImportDialog.quickImport"));
        JButton titleAndTimeSignatures =
                DialogStyle.flatButton(Texts.get("score_dialogs.MidiImportDialog.importTitleAndTimeSignatures"));
        JButton addTrack = DialogStyle.flatButton(Texts.get("score_dialogs.shared.addTrack"));
        JButton importOntoCurrent =
                DialogStyle.flatButton(Texts.get("score_dialogs.MidiImportDialog.importOntoCurrentTrack"));

        JPanel top = flowOf(openAnother);
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

        quickImport.addActionListener(event -> withSelection(parent, panel, selected -> {
            if (!askToDiscardChanges.getAsBoolean()) {
                return;
            }
            try {
                Score imported = exchange.importMidiQuick(
                        currentPath[0], selected, panel.transposeDownOneOctave(), Optional.of(panel.chordPositionQuantize()),
                        Optional.of(panel.noteDurationQuantize()), panel.useTwoChannelsPerTrack());
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
                Optional<NoteValue> chordPositionQuantize = Optional.of(panel.chordPositionQuantize());
                Optional<NoteValue> noteDurationQuantize = Optional.of(panel.noteDurationQuantize());
                editor.apply(score -> score.mappingTrack(
                        trackIndex,
                        track -> exchange.importMidiInto(
                                track, currentPath[0], selected, transpose, chordPositionQuantize, noteDurationQuantize)));
                afterChange.run();
            } catch (ScoreFileException e) {
                showError(parent, e);
            }
        }));

        DialogShell.show(parent, Texts.get("score_dialogs.MidiImportDialog.title"), content);
    }

    private static void withSelection(Component parent, MidiImportPanel panel, Consumer<List<Integer>> action) {
        List<Integer> selected = panel.selectedTrackIndices();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent, Texts.get("score_dialogs.MidiImportDialog.selectAtLeastOneTrack"), "tabpro",
                    JOptionPane.INFORMATION_MESSAGE);
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
        return new FileNameExtensionFilter(Texts.get("score_dialogs.MidiImportDialog.fileFilter"), "mid", "midi");
    }

    private static void showError(Component parent, ScoreFileException e) {
        JOptionPane.showMessageDialog(parent, ErrorTexts.of(e), "tabpro", JOptionPane.ERROR_MESSAGE);
    }

    private static Timeline timelineOrSilence(Component parent, ScoreExchange exchange, Path path, List<Integer> selected) {
        try {
            return exchange.midiTrackTimeline(path, selected);
        } catch (ScoreFileException e) {
            showError(parent, e);
            return silentTimeline();
        }
    }

    private static Timeline silentTimeline() {
        return new Timeline(0, Duration.TICKS_PER_QUARTER, List.of());
    }
}
