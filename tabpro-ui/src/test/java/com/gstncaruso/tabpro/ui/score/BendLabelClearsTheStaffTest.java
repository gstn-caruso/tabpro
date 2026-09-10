package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

class BendLabelClearsTheStaffTest {

    private static final int WIDTH = 300;

    @Test
    void aFullBendOnTheFirstStringDoesNotBleedIntoTheStaff() {
        Note bent = new Note(1, 7).withBend(Bend.of(BendType.BEND, 4));
        Painted withBend = paint(measureWith(bent));
        Painted plain = paint(measureWith(new Note(1, 7)));

        Rectangle staffArea = new Rectangle(
                plain.layout.measureX(0), plain.layout.staffTop(0, 0),
                plain.layout.measureWidth(0), ScoreLayout.STAFF_HEIGHT);

        Rectangle gapArea = new Rectangle(
                plain.layout.measureX(0), plain.layout.staffBottom(0, 0),
                plain.layout.measureWidth(0), ScoreLayout.STAFF_TO_TAB_GAP);
        assertTrue(withBend.inkIn(gapArea) > plain.inkIn(gapArea), "el bend tiene que dibujar algo en la brecha");
        assertEquals(plain.inkIn(staffArea), withBend.inkIn(staffArea),
                "la curva y la etiqueta \"full\" del bend no pueden pintar tinta dentro del pentagrama");
    }

    private static Measure measureWith(Note note) {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), note),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter())));
    }

    private static Painted paint(Measure measure) {
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH);
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ScorePainter.paint(g, layout, score, new Cursor(0, 0, 0, 0), Playhead.silent());
        g.dispose();
        return new Painted(image, layout);
    }

    private record Painted(BufferedImage image, ScoreLayout layout) {
        int inkIn(Rectangle area) {
            int ink = 0;
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    if (image.getRGB(x, y) != ScoreColors.BACKGROUND.getRGB()) {
                        ink++;
                    }
                }
            }
            return ink;
        }
    }
}
