package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.util.OptionalInt;
import javax.swing.JPanel;

/**
 * La vista general: la zona de marcadores arriba y, debajo, la grilla de compases por pista. Las
 * dos comparten el mismo ancho de celda, asi que se ven y se scrollean como una sola unidad.
 */
public final class GlobalView extends JPanel {

    private final MarkerZone markerZone;
    private final MeasureGrid grid;

    public GlobalView(Editor editor) {
        this.markerZone = new MarkerZone(editor);
        this.grid = new MeasureGrid(editor);
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
}
