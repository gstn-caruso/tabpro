package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public final class LyricsPanel extends FormPanel {

    private final JComboBox<String> trackChooser;
    private final List<LyricLineRow> lines = new ArrayList<>();
    private JTextArea lastFocused;

    public LyricsPanel(List<String> trackNames, Lyrics initial) {
        trackChooser = new JComboBox<>(trackNames.toArray(new String[0]));
        trackChooser.setSelectedIndex(clampedTrack(initial.trackIndex(), trackNames.size()));
        addRow(Texts.get("score_dialogs.LyricsPanel.track"), trackChooser);

        addFullWidthRow(new JLabel(Texts.get("score_dialogs.LyricsPanel.syllableHint")));

        JTabbedPane lineTabs = new JTabbedPane();
        for (int index = 0; index < LyricLine.MAX_LINES; index++) {
            LyricLineRow row = new LyricLineRow(initial.line(index), index + 1);
            row.textArea().addFocusListener(rememberingFocus(row.textArea()));
            lines.add(row);
            lineTabs.addTab(Texts.get("score_dialogs.shared.line", index + 1), row);
        }
        addFullWidthRow(lineTabs);

        addFullWidthRow(cutCopyPasteBar());
    }

    private FocusAdapter rememberingFocus(JTextArea field) {
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                lastFocused = field;
            }
        };
    }

    private javax.swing.JPanel cutCopyPasteBar() {
        javax.swing.JButton cut = DialogStyle.flatButton(Texts.get("score_dialogs.LyricsPanel.cut"));
        javax.swing.JButton copy = DialogStyle.flatButton(Texts.get("score_dialogs.LyricsPanel.copy"));
        javax.swing.JButton paste = DialogStyle.flatButton(Texts.get("score_dialogs.LyricsPanel.paste"));
        cut.addActionListener(event -> onFocusedField(JTextArea::cut));
        copy.addActionListener(event -> onFocusedField(JTextArea::copy));
        paste.addActionListener(event -> onFocusedField(JTextArea::paste));

        javax.swing.JPanel bar = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, DialogStyle.GAP_S, 0));
        bar.setOpaque(false);
        bar.add(cut);
        bar.add(copy);
        bar.add(paste);
        return bar;
    }

    private void onFocusedField(java.util.function.Consumer<JTextArea> action) {
        if (lastFocused != null) {
            action.accept(lastFocused);
        }
    }

    private static int clampedTrack(int trackIndex, int trackCount) {
        if (trackCount == 0) {
            return -1;
        }
        return Math.max(0, Math.min(trackIndex, trackCount - 1));
    }

    public int selectedTrackIndex() {
        return trackChooser.getSelectedIndex();
    }

    public void selectTrack(int index) {
        trackChooser.setSelectedIndex(index);
    }

    public void setLine(int index, LyricLine line) {
        lines.get(index).apply(line);
    }

    public LyricLine line(int index) {
        return lines.get(index).toLyricLine();
    }

    public Lyrics toLyrics() {
        List<LyricLine> collected = lines.stream().map(LyricLineRow::toLyricLine).toList();
        return new Lyrics(Math.max(0, selectedTrackIndex()), collected);
    }
}
