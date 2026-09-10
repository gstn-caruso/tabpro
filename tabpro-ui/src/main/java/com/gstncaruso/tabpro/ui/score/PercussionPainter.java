package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** La notacion propia de percusion: cada linea es un sonido y la cabeza cambia de forma segun
 * de que sonido se trata (platillos con X, panderetas y afines con rombo, el resto con ovalo). */
final class PercussionPainter {

    private static final int RADIUS = 4;

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
        String name = PercussionKit.nameOf(sound).orElse("").toLowerCase(java.util.Locale.ROOT);
        if (name.contains("hi-hat") || name.contains("crash") || name.contains("ride")
                || name.contains("platillo") || name.contains("campana") || name.contains("silbato")) {
            return Shape.CROSS;
        }
        if (name.contains("pandereta") || name.contains("triangulo") || name.contains("cencerro")
                || name.contains("claves") || name.contains("agogo") || name.contains("cabasa")
                || name.contains("maracas") || name.contains("guiro") || name.contains("cuica")
                || name.contains("vibraslap")) {
            return Shape.DIAMOND;
        }
        return Shape.OVAL;
    }

    private enum Shape {
        OVAL, CROSS, DIAMOND
    }
}
