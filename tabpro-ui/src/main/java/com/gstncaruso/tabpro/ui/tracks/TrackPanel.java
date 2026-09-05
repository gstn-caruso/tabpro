package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * El panel de abajo: todas las pistas listadas con su mixer y, al costado, la grilla de compases
 * con el que esta sonando en rojo.
 */
public final class TrackPanel extends JPanel {

    public static final int ROW_HEIGHT = 30;
    public static final int HEADER_HEIGHT = 22;
    public static final int ICON_WIDTH = 22;
    public static final int NAME_WIDTH = 106;
    public static final int INSTRUMENT_WIDTH = 158;
    public static final int SLIDER_WIDTH = 78;
    public static final int TOGGLE_WIDTH = 26;
    public static final int COLUMN_GAP = 6;
    public static final int MIXER_WIDTH = ICON_WIDTH + NAME_WIDTH + INSTRUMENT_WIDTH
            + 2 * SLIDER_WIDTH + 2 * TOGGLE_WIDTH + 7 * COLUMN_GAP + 16;

    private final Editor editor;
    private final JPanel strips = new JPanel();
    private final MeasureGrid grid;
    private final List<TrackStrip> rows = new ArrayList<>();

    public TrackPanel(Editor editor) {
        this.editor = editor;
        this.grid = new MeasureGrid(editor);
        setLayout(new BorderLayout());
        setBackground(ScoreColors.SURFACE);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ScoreColors.BORDER));

        strips.setLayout(new BoxLayout(strips, BoxLayout.Y_AXIS));
        strips.setBackground(ScoreColors.SURFACE);

        JPanel mixer = new JPanel(new BorderLayout());
        mixer.setBackground(ScoreColors.SURFACE);
        mixer.add(header(), BorderLayout.NORTH);
        mixer.add(strips, BorderLayout.CENTER);
        mixer.setPreferredSize(new Dimension(MIXER_WIDTH, 0));

        JScrollPane scrollingGrid = new JScrollPane(grid);
        scrollingGrid.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ScoreColors.BORDER));
        scrollingGrid.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollingGrid.getViewport().setBackground(ScoreColors.SURFACE);

        add(mixer, BorderLayout.WEST);
        add(scrollingGrid, BorderLayout.CENTER);

        rebuild();
        editor.addListener(this::editorChanged);
    }

    public void showPlayingMeasure(OptionalInt measure) {
        grid.showPlayingMeasure(measure);
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

    private JComponent header() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(ScoreColors.SURFACE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        header.setPreferredSize(new Dimension(MIXER_WIDTH, HEADER_HEIGHT));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT));
        addTitle(header, "", ICON_WIDTH);
        addTitle(header, "Pista", NAME_WIDTH);
        addTitle(header, "Instrumento", INSTRUMENT_WIDTH);
        addTitle(header, "Volumen", SLIDER_WIDTH);
        addTitle(header, "Paneo", SLIDER_WIDTH);
        addTitle(header, "M", TOGGLE_WIDTH);
        addTitle(header, "S", TOGGLE_WIDTH);
        return header;
    }

    private void addTitle(JPanel header, String text, int width) {
        JLabel title = new JLabel(text);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 10f));
        title.setForeground(ScoreColors.MUTED_INK);
        Dimension size = new Dimension(width, HEADER_HEIGHT);
        title.setPreferredSize(size);
        title.setMaximumSize(size);
        title.setMinimumSize(size);
        header.add(title);
        header.add(Box.createHorizontalStrut(COLUMN_GAP));
    }

    private void editorChanged() {
        if (rows.size() != editor.score().trackCount()) {
            rebuild();
            return;
        }
        rows.forEach(TrackStrip::refresh);
        grid.revalidate();
        grid.repaint();
    }

    private void rebuild() {
        strips.removeAll();
        rows.clear();
        for (int trackIndex = 0; trackIndex < editor.score().trackCount(); trackIndex++) {
            TrackStrip strip = new TrackStrip(editor, trackIndex);
            rows.add(strip);
            strips.add(strip);
        }
        strips.add(Box.createVerticalGlue());
        strips.revalidate();
        strips.repaint();
        grid.revalidate();
        grid.repaint();
    }

    /** Alto que pide el panel para mostrar todas sus pistas sin scrollear. */
    public int preferredPanelHeight() {
        return HEADER_HEIGHT + editor.score().trackCount() * ROW_HEIGHT + 26;
    }

    List<TrackStrip> rows() {
        return List.copyOf(rows);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        return new Dimension(preferred.width, Math.min(preferredPanelHeight(), 220));
    }

    Component gridComponent() {
        return grid;
    }
}
