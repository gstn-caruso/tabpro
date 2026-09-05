package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Dibuja la partitura entera: cada pista con su pentagrama arriba y su tablatura abajo, una
 * debajo de la otra, sistema por sistema.
 */
public final class ScorePainter {

    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    private ScorePainter() {
    }

    public static void paint(Graphics2D g, ScoreLayout layout, Score score, Cursor cursor, Playhead playhead) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        paintBackground(g);

        for (int trackIndex = 0; trackIndex < score.trackCount(); trackIndex++) {
            paintTrack(g, layout, score, trackIndex, cursor, playhead);
        }
        paintCursor(g, layout, cursor);
    }

    private static void paintBackground(Graphics2D g) {
        Rectangle clip = g.getClipBounds();
        g.setColor(ScoreColors.BACKGROUND);
        g.fill(clip == null ? new Rectangle(0, 0, 4000, 4000) : clip);
    }

    private static void paintTrack(
            Graphics2D g, ScoreLayout layout, Score score, int trackIndex, Cursor cursor, Playhead playhead) {
        Track track = score.track(trackIndex);
        Clef clef = Clef.forTuning(track.tuning());
        boolean selected = cursor.track() == trackIndex;

        playhead.on(trackIndex).ifPresent(position -> paintPlaying(g, layout, trackIndex, position));

        for (int measureIndex = 0; measureIndex < track.measureCount(); measureIndex++) {
            StaffPainter.paintStaffLines(g, layout, trackIndex, measureIndex);
            TabPainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
            if (layout.startsASystem(measureIndex)) {
                paintTrackLabel(g, layout, track, trackIndex, measureIndex, selected);
                StaffPainter.paintClef(g, layout, clef, trackIndex, measureIndex);
                StaffPainter.paintTimeSignature(g, layout, track, trackIndex, measureIndex,
                        layout.measureX(measureIndex) + ScoreLayout.SYSTEM_HEAD_WIDTH - 20);
                TabPainter.paintTabMark(g, layout, track, trackIndex, measureIndex);
            }
            TabPainter.paintMeasureNumber(g, layout, track, trackIndex, measureIndex);
            StaffPainter.paintMeasure(g, layout, track, clef, trackIndex, measureIndex);
            TabPainter.paintFrets(g, layout, track, trackIndex, measureIndex);
            TabNotationPainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
            TabSymbolPainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
        }
    }

    private static void paintTrackLabel(
            Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex, boolean selected) {
        g.setFont(LABEL_FONT);
        g.setColor(selected ? ScoreColors.INK : ScoreColors.LABEL);
        int y = layout.trackTop(trackIndex, measureIndex) + ScoreLayout.TRACK_LABEL_HEIGHT - 4;
        int x = layout.measureX(measureIndex);
        if (selected) {
            g.setColor(ScoreColors.ACCENT);
            g.fillRect(x - 8, y - 9, 3, 11);
            g.setColor(ScoreColors.INK);
        }
        g.drawString(track.name(), x, y);
    }

    private static void paintPlaying(Graphics2D g, ScoreLayout layout, int trackIndex, BeatPosition position) {
        Rectangle beat = layout.beatBounds(trackIndex, position.measure(), position.beat());
        int top = layout.staffTop(trackIndex, position.measure());
        g.setColor(ScoreColors.PLAYING);
        g.fillRect(beat.x, top, beat.width, layout.tabBottom(trackIndex, position.measure()) - top);
    }

    private static void paintCursor(Graphics2D g, ScoreLayout layout, Cursor cursor) {
        Rectangle beat = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat());
        int y = layout.stringY(cursor.track(), cursor.measure(), cursor.string())
                - ScoreLayout.STRING_SPACING / 2 + 1;
        g.setColor(cursorColor());
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawRect(beat.x + 1, y, beat.width - 2, ScoreLayout.STRING_SPACING - 2);
    }

    private static Color cursorColor() {
        return ScoreColors.CURSOR;
    }
}
