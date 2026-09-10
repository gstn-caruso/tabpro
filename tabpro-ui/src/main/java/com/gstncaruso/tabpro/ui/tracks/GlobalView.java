package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.util.OptionalInt;
import javax.swing.JPanel;

public final class GlobalView extends JPanel {

    private final MarkerZone markerZone;
    private final MeasureGrid grid;

    public GlobalView(Editor editor) {
        this(new MarkerZone(editor), new MeasureGrid(editor));
    }

    GlobalView(MarkerZone markerZone, MeasureGrid grid) {
        this.markerZone = markerZone;
        this.grid = grid;
        setLayout(new BorderLayout());
        setBackground(ScoreColors.SURFACE);
        add(markerZone, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    public MeasureGrid grid() {
        return grid;
    }

    public void showPlayingMeasure(OptionalInt measure) {
        grid.showPlayingMeasure(measure);
    }

    public void refresh() {
        markerZone.revalidate();
        markerZone.repaint();
        grid.revalidate();
        grid.repaint();
    }

    public void moveCursorHighlight() {
        grid.moveCursorHighlight();
    }
}
