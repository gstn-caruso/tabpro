package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/** La letra de la pista que la lleva, repartida silaba por silaba debajo de su tablatura. */
final class LyricsPainter {

    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private static final int LINE_HEIGHT = 13;
    private static final int FIRST_LINE_GAP = 12;

    private LyricsPainter() {
    }

    static void paintTrack(Graphics2D g, ScoreLayout layout, Score score, int trackIndex) {
        Lyrics lyrics = score.lyrics();
        if (lyrics.trackIndex() != trackIndex || lyrics.isEmpty()) {
            return;
        }
        Track track = score.track(trackIndex);
        for (int lineIndex = 0; lineIndex < LyricLine.MAX_LINES; lineIndex++) {
            LyricLine line = lyrics.line(lineIndex);
            if (!line.isEmpty()) {
                paintLine(g, layout, track, trackIndex, line, lineIndex);
            }
        }
    }

    private static void paintLine(
            Graphics2D g, ScoreLayout layout, Track track, int trackIndex, LyricLine line, int lineIndex) {
        List<String> syllables = line.syllables();
        int measureIndex = line.startingMeasure() - 1;
        int beatIndex = 0;
        int syllableIndex = 0;

        while (syllableIndex < syllables.size() && measureIndex < track.measureCount()) {
            Measure measure = track.measure(measureIndex);
            if (beatIndex >= measure.beats().size()) {
                measureIndex++;
                beatIndex = 0;
                continue;
            }
            String syllable = syllables.get(syllableIndex);
            if (!syllable.isBlank()) {
                paintSyllable(g, layout, trackIndex, measureIndex, beatIndex, syllable, lineIndex);
            }
            beatIndex++;
            syllableIndex++;
        }
    }

    private static void paintSyllable(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, String syllable,
            int lineIndex) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        int centerX = bounds.x + bounds.width / 2;
        int y = layout.tabBottom(trackIndex, measureIndex) + FIRST_LINE_GAP + lineIndex * LINE_HEIGHT;

        g.setFont(FONT);
        g.setColor(ScoreColors.INK);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(syllable, centerX - metrics.stringWidth(syllable) / 2, y);
    }
}
