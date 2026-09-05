package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.OptionalInt;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * El panel de abajo: la mesa de mezcla con todas las pistas y, al costado, la vista general con
 * los marcadores y la grilla de compases.
 */
public final class TrackPanel extends JPanel {

    public static final int ROW_HEIGHT = 34;
    public static final int HEADER_HEIGHT = 22;

    private final Editor editor;
    private final MixTable mixTable;
    private final GlobalView globalView;

    public TrackPanel(Editor editor) {
        this.editor = editor;
        this.mixTable = new MixTable(editor);
        this.globalView = new GlobalView(editor);
        setLayout(new BorderLayout());
        setBackground(ScoreColors.SURFACE);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ScoreColors.BORDER));

        JScrollPane scrollingMixer = new JScrollPane(mixTable);
        scrollingMixer.setBorder(BorderFactory.createEmptyBorder());
        scrollingMixer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollingMixer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollingMixer.getViewport().setBackground(ScoreColors.SURFACE);
        scrollingMixer.setPreferredSize(new Dimension(MixTable.WIDTH, 0));

        JScrollPane scrollingGlobalView = new JScrollPane(globalView);
        scrollingGlobalView.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ScoreColors.BORDER));
        scrollingGlobalView.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollingGlobalView.getViewport().setBackground(ScoreColors.SURFACE);

        add(scrollingMixer, BorderLayout.WEST);
        add(scrollingGlobalView, BorderLayout.CENTER);

        editor.addListener(this::editorChanged);
    }

    public void showPlayingMeasure(OptionalInt measure) {
        globalView.showPlayingMeasure(measure);
    }

    public void addGuitar() {
        editor.addTrack(Track.standardGuitar(freshName("Guitarra")));
    }

    public void addBass() {
        editor.addTrack(Track.standardBass(freshName("Bajo")));
    }

    public void removeSelectedTrack() {
        if (editor.score().trackCount() == 1) {
            JOptionPane.showMessageDialog(
                    this, "Una partitura necesita al menos una pista.", "tabpro", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        editor.removeCurrentTrack();
    }

    public void renameSelectedTrack() {
        renameTrack(this, editor, editor.cursor().track());
    }

    static void renameTrack(JComponent parent, Editor editor, int trackIndex) {
        String current = editor.score().track(trackIndex).name();
        String chosen = JOptionPane.showInputDialog(parent, "Nombre de la pista", current);
        if (chosen != null && !chosen.isBlank()) {
            editor.renameTrack(trackIndex, chosen.trim());
        }
    }

    private String freshName(String base) {
        long sameBase = editor.score().tracks().stream()
                .filter(track -> track.name().startsWith(base))
                .count();
        return sameBase == 0 ? base : base + " " + (sameBase + 1);
    }

    private void editorChanged() {
        mixTable.refresh();
        globalView.refresh();
    }

    /** Alto que pide el panel para mostrar todas sus pistas sin scrollear. */
    public int preferredPanelHeight() {
        return HEADER_HEIGHT + editor.score().trackCount() * ROW_HEIGHT + 26;
    }

    List<MixTableRow> rows() {
        return mixTable.rows();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        return new Dimension(preferred.width, Math.min(preferredPanelHeight(), 220));
    }

    Component gridComponent() {
        return globalView.grid();
    }
}
