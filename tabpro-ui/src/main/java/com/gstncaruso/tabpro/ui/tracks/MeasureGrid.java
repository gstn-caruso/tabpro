package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.OptionalInt;
import javax.swing.JComponent;

/**
 * Un cuadradito por compas y por pista: marcado si esa pista toca algo ahi, y toda la columna
 * en rojo mientras ese compas suena.
 */
public final class MeasureGrid extends JComponent {

    public static final int CELL_WIDTH = 15;
    public static final int NUMBER_EVERY = 5;

    private static final Color PLAYING_TINT = new Color(
            ScoreColors.PLAYING_MEASURE.getRed(),
            ScoreColors.PLAYING_MEASURE.getGreen(),
            ScoreColors.PLAYING_MEASURE.getBlue(),
            60);

    private final Editor editor;
    private OptionalInt playingMeasure = OptionalInt.empty();

    public MeasureGrid(Editor editor) {
        this.editor = editor;
        setOpaque(true);
        setBackground(ScoreColors.SURFACE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hitTest(e.getX(), e.getY()).ifPresent(MeasureGrid.this::goTo);
            }
        });
    }

    public void showPlayingMeasure(OptionalInt measure) {
        this.playingMeasure = measure;
        repaint();
    }

    public Rectangle cellBounds(int track, int measure) {
        return new Rectangle(
                measure * CELL_WIDTH,
                TrackPanel.HEADER_HEIGHT + track * TrackPanel.ROW_HEIGHT,
                CELL_WIDTH,
                TrackPanel.ROW_HEIGHT);
    }

    public Optional<Cell> hitTest(int x, int y) {
        Score score = editor.score();
        int measure = x / CELL_WIDTH;
        int track = (y - TrackPanel.HEADER_HEIGHT) / TrackPanel.ROW_HEIGHT;
        boolean inside = x >= 0
                && y >= TrackPanel.HEADER_HEIGHT
                && measure < score.measureCount()
                && track >= 0
                && track < score.trackCount();
        return inside ? Optional.of(new Cell(track, measure)) : Optional.empty();
    }

    @Override
    public Dimension getPreferredSize() {
        Score score = editor.score();
        return new Dimension(
                Math.max(1, score.measureCount()) * CELL_WIDTH,
                TrackPanel.HEADER_HEIGHT + score.trackCount() * TrackPanel.ROW_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Score score = editor.score();

        g.setColor(ScoreColors.SURFACE);
        g.fillRect(0, 0, getWidth(), getHeight());

        playingMeasure.ifPresent(measure -> tintPlayingColumn(g, score, measure));
        paintMeasureNumbers(g, score);

        for (int track = 0; track < score.trackCount(); track++) {
            paintRow(g, score.track(track), track, track == editor.cursor().track());
        }
        playingMeasure.ifPresent(measure -> outlinePlayingColumn(g, score, measure));
    }

    private void tintPlayingColumn(Graphics2D g, Score score, int measure) {
        if (isOutside(score, measure)) {
            return;
        }
        g.setColor(PLAYING_TINT);
        g.fillRect(measure * CELL_WIDTH, 0, CELL_WIDTH, getHeight());
    }

    /** El borde va despues de las celdas, para que marque la columna sin taparla. */
    private void outlinePlayingColumn(Graphics2D g, Score score, int measure) {
        if (isOutside(score, measure)) {
            return;
        }
        g.setColor(ScoreColors.PLAYING_MEASURE);
        g.setStroke(new BasicStroke(2));
        g.drawRect(measure * CELL_WIDTH + 1, 1, CELL_WIDTH - 2, getHeight() - 2);
    }

    private boolean isOutside(Score score, int measure) {
        return measure < 0 || measure >= score.measureCount();
    }

    private void paintMeasureNumbers(Graphics2D g, Score score) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g.setColor(ScoreColors.MUTED_INK);
        for (int measure = 0; measure < score.measureCount(); measure++) {
            if (measure == 0 || (measure + 1) % NUMBER_EVERY == 0) {
                g.drawString(String.valueOf(measure + 1), measure * CELL_WIDTH + 2, TrackPanel.HEADER_HEIGHT - 7);
            }
        }
    }

    /** Una pista que no suena se ve apagada, igual que su nombre en la lista. */
    private Color colorOf(int trackIndex) {
        Color color = TrackColors.of(trackIndex);
        return editor.score().isAudible(trackIndex) ? color : faded(color);
    }

    private static Color faded(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 70);
    }

    private void paintRow(Graphics2D g, Track track, int trackIndex, boolean selected) {
        for (int measure = 0; measure < editor.score().measureCount(); measure++) {
            Rectangle cell = cellBounds(trackIndex, measure);
            if (measure >= track.measureCount()) {
                continue;
            }
            g.setColor(selected ? ScoreColors.SURFACE_HIGHLIGHT : ScoreColors.SURFACE);
            g.fillRect(cell.x + 1, cell.y + 1, cell.width - 2, cell.height - 2);
            g.setColor(ScoreColors.BORDER);
            g.drawRect(cell.x + 1, cell.y + 1, cell.width - 3, cell.height - 3);
            if (track.hasNotesIn(measure)) {
                g.setColor(colorOf(trackIndex));
                g.fillRect(cell.x + 4, cell.y + 4, cell.width - 8, cell.height - 8);
            }
        }
    }

    private void goTo(Cell cell) {
        if (cell.track() != editor.cursor().track()) {
            editor.selectTrack(cell.track());
        }
        if (cell.measure() < editor.currentTrack().measureCount()) {
            editor.moveTo(cell.measure(), 0, editor.cursor().string());
        }
    }

    public record Cell(int track, int measure) {}
}
