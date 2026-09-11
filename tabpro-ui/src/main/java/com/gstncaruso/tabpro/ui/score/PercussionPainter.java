package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Set;

final class PercussionPainter {

    private static final int RADIUS = 4;

    private static final Set<Integer> SOUNDS_WITH_A_CROSS_NOTEHEAD = Set.of(42, 44, 46, 49, 51, 52, 53, 57, 59, 71, 72);
    private static final Set<Integer> SOUNDS_WITH_A_DIAMOND_NOTEHEAD =
            Set.of(54, 56, 58, 67, 68, 69, 70, 73, 74, 75, 78, 79, 80, 81);

    private PercussionPainter() {
    }

    static void paintMeasure(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        Measure measure = track.measure(measureIndex);
        for (int beatIndex = 0; beatIndex < measure.beats().size(); beatIndex++) {
            Beat beat = measure.beat(beatIndex);
            for (Note note : beat.notes()) {
                paintNotehead(g, layout, trackIndex, measureIndex, beatIndex, note);
            }
        }
    }

    private static void paintNotehead(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Note note) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        int centerX = bounds.x + bounds.width / 2;
        int y = layout.stringY(trackIndex, measureIndex, note.string());

        g.setColor(ScoreColors.BACKGROUND);
        g.fillRect(centerX - RADIUS - 2, y - RADIUS - 2, (RADIUS + 2) * 2, (RADIUS + 2) * 2);

        g.setColor(ScoreColors.INK);
        switch (shapeFor(note.fret())) {
            case CROSS -> paintGlyphNotehead(g, centerX, y, MusicFont.noteheadXBlack());
            case DIAMOND -> paintGlyphNotehead(g, centerX, y, MusicFont.noteheadDiamondBlack());
            default -> paintGlyphNotehead(g, centerX, y, MusicFont.noteheadBlack());
        }
    }

    private static void paintGlyphNotehead(Graphics2D g, int centerX, int y, String glyph) {
        g.setFont(MusicFont.sizedTo(ScoreLayout.STAFF_LINE_SPACING));
        double width = g.getFontMetrics().stringWidth(glyph);
        g.drawString(glyph, (float) (centerX - width / 2), y);
    }

    private static Shape shapeFor(int sound) {
        if (SOUNDS_WITH_A_CROSS_NOTEHEAD.contains(sound)) {
            return Shape.CROSS;
        }
        if (SOUNDS_WITH_A_DIAMOND_NOTEHEAD.contains(sound)) {
            return Shape.DIAMOND;
        }
        return Shape.OVAL;
    }

    private enum Shape {
        OVAL, CROSS, DIAMOND
    }
}
