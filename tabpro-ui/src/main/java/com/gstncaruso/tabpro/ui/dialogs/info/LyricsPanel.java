package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * La letra de la cancion sobre una pista elegida, hasta cinco lineas cada una con
 * su compas inicial. Explica la sintaxis de silabas: espacio o guion separan,
 * mas une, y lo que va entre corchetes no se dibuja.
 */
public final class LyricsPanel extends FormPanel {

    private final JComboBox<String> trackChooser;
    private final List<LyricLineRow> lines = new ArrayList<>();
    private JTextField lastFocused;

    public LyricsPanel(List<String> trackNames, Lyrics initial) {
        trackChooser = new JComboBox<>(trackNames.toArray(new String[0]));
        trackChooser.setSelectedIndex(clampedTrack(initial.trackIndex(), trackNames.size()));
        addRow("Pista", trackChooser);

        addFullWidthRow(new JLabel(
                "<html>Silabas: separadas con espacio o guion. Un + une dos palabras."
                        + " Lo que va entre corchetes [asi] no se dibuja.</html>"));

        for (int index = 0; index < LyricLine.MAX_LINES; index++) {
            LyricLineRow row = new LyricLineRow(initial.line(index));
            row.textField().addFocusListener(rememberingFocus(row.textField()));
            lines.add(row);
            addRow("Linea " + (index + 1), row);
        }

        addFullWidthRow(cutCopyPasteBar());
    }

    private FocusAdapter rememberingFocus(JTextField field) {
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                lastFocused = field;
            }
        };
    }

    private javax.swing.JPanel cutCopyPasteBar() {
        javax.swing.JButton cut = DialogStyle.flatButton("Cortar");
        javax.swing.JButton copy = DialogStyle.flatButton("Copiar");
        javax.swing.JButton paste = DialogStyle.flatButton("Pegar");
        cut.addActionListener(event -> onFocusedField(JTextField::cut));
        copy.addActionListener(event -> onFocusedField(JTextField::copy));
        paste.addActionListener(event -> onFocusedField(JTextField::paste));

        javax.swing.JPanel bar = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, DialogStyle.GAP_S, 0));
        bar.setOpaque(false);
        bar.add(cut);
        bar.add(copy);
        bar.add(paste);
        return bar;
    }

    private void onFocusedField(java.util.function.Consumer<JTextField> action) {
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

    /** Cambia una linea puntual, para poblar el formulario o para probarlo. */
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
