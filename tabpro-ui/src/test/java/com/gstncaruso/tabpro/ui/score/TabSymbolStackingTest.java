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
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Fade in, P.M. y let ring se apilan sobre la tablatura con {@link
 * com.gstncaruso.tabpro.core.notation.VerticalStack}: cuantos mas coinciden en el mismo beat, mas
 * cerca del pentagrama sube la fila de arriba. Achicar {@link ScoreLayout#STAFF_TO_TAB_GAP} deja
 * menos aire para ese apilamiento, y no puede empezar a pintar dentro del pentagrama.
 */
class TabSymbolStackingTest {

    private static final int WIDTH = 300;

    @Test
    void threeStackedLabelsDoNotBleedIntoTheStaff() {
        Note fretted = new Note(3, 5).toggling(Ornament.PALM_MUTE).toggling(Ornament.LET_RING);
        Painted withSymbols = paint(measureWith(fretted, BeatEffects.none().withFadeIn(true)));
        Painted plain = paint(measureWith(new Note(3, 5), BeatEffects.none()));

        Rectangle staffArea = new Rectangle(
                plain.layout.measureX(0), plain.layout.staffTop(0, 0),
                plain.layout.measureWidth(0), ScoreLayout.STAFF_HEIGHT);

        assertEquals(plain.inkIn(staffArea), withSymbols.inkIn(staffArea),
                "fade in, P.M. y let ring apilados no pueden pintar tinta dentro del pentagrama");
    }

    @Test
    void aSingleLabelSitsBetweenTheStaffAndTheTablature() {
        Note fretted = new Note(3, 5).toggling(Ornament.PALM_MUTE);
        Painted withLabel = paint(measureWith(fretted, BeatEffects.none()));
        Painted plain = paint(measureWith(new Note(3, 5), BeatEffects.none()));

        Rectangle staffArea = new Rectangle(
                plain.layout.measureX(0), plain.layout.staffTop(0, 0),
                plain.layout.measureWidth(0), ScoreLayout.STAFF_HEIGHT);
        Rectangle gapArea = new Rectangle(
                plain.layout.measureX(0), plain.layout.staffBottom(0, 0),
                plain.layout.measureWidth(0), ScoreLayout.STAFF_TO_TAB_GAP);
        Rectangle tabLineArea = new Rectangle(
                plain.layout.measureX(0), plain.layout.tabTop(0, 0) - 1,
                plain.layout.measureWidth(0), 3);

        assertTrue(withLabel.inkIn(gapArea) > plain.inkIn(gapArea), "P.M. tiene que dibujarse en la brecha");
        assertEquals(plain.inkIn(staffArea), withLabel.inkIn(staffArea),
                "P.M. no puede pintar tinta dentro del pentagrama");
        assertEquals(plain.inkIn(tabLineArea), withLabel.inkIn(tabLineArea),
                "P.M. no puede pintar tinta sobre la primera linea de la tablatura");
    }

    private static Measure measureWith(Note note, BeatEffects effects) {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), note).withEffects(effects),
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
