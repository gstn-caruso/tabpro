package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FretboardViewTest {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 120;

    @Test
    void spreadsTheStringsFromTheHighestAtTheTop() {
        FretboardView view = sized(new FretboardView());
        view.show(Tuning.standard(), Beat.rest(Duration.quarter()));

        assertTrue(view.stringY(1) < view.stringY(6), "la primera cuerda va arriba");
        assertEquals(
                view.stringY(2) - view.stringY(1),
                view.stringY(6) - view.stringY(5),
                "las cuerdas van parejas");
    }

    @Test
    void aBassSpreadsItsFourStringsOverTheSameNeck() {
        FretboardView view = sized(new FretboardView());
        view.show(Tuning.standardBass(), Beat.rest(Duration.quarter()));

        assertEquals(4, view.stringCount());
        assertTrue(view.stringY(4) > view.stringY(1));
    }

    @Test
    void putsTheOpenStringsBeforeTheNut() {
        FretboardView view = sized(new FretboardView());
        view.show(Tuning.standard(), Beat.rest(Duration.quarter()));

        assertTrue(view.fretCenterX(0) < view.nutX());
        assertTrue(view.fretCenterX(1) > view.nutX());
    }

    @Test
    void placesEachFretAfterTheOneBefore() {
        FretboardView view = sized(new FretboardView());
        view.show(Tuning.standard(), Beat.rest(Duration.quarter()));

        for (int fret = 1; fret < FretboardView.FRETS; fret++) {
            assertTrue(
                    view.fretCenterX(fret) < view.fretCenterX(fret + 1),
                    "el traste " + fret + " tiene que estar antes que el siguiente");
        }
    }

    @Test
    void reachesTheLastFretOfAStandardInstrument() {
        assertEquals(TrackSettings.DEFAULT_FRET_COUNT, FretboardView.FRETS);
    }

    @Test
    void marksTheNotesOfTheBeatAndNothingElse() {
        FretboardView view = sized(new FretboardView());
        Beat chord = Beat.of(Duration.quarter(), new Note(6, 3), new Note(1, 0));

        view.show(Tuning.standard(), chord);
        BufferedImage image = paint(view);

        assertTrue(hasMarkNear(image, view.fretCenterX(3), view.stringY(6)), "falta la nota del traste 3");
        assertTrue(hasMarkNear(image, view.fretCenterX(0), view.stringY(1)), "falta la cuerda al aire");
        assertTrue(!hasMarkNear(image, view.fretCenterX(7), view.stringY(4)), "marco una nota que no suena");
    }

    @Test
    void aRestLeavesTheNeckBare() {
        FretboardView view = sized(new FretboardView());

        view.show(Tuning.standard(), Beat.rest(Duration.quarter()));
        BufferedImage image = paint(view);

        for (int string = 1; string <= 6; string++) {
            assertTrue(
                    !hasMarkNear(image, view.fretCenterX(5), view.stringY(string)),
                    "un silencio no tiene que marcar nada");
        }
    }

    @Test
    void readsBackTheNoteUnderEachFretAndString() {
        FretboardView view = sized(new FretboardView());
        view.show(Tuning.standard(), Beat.rest(Duration.quarter()));

        for (int string = 1; string <= 6; string++) {
            for (int fret = 0; fret <= FretboardView.FRETS; fret++) {
                assertEquals(
                        Optional.of(new Note(string, fret)),
                        view.noteAt(view.fretCenterX(fret), view.stringY(string)),
                        "el punto de la cuerda " + string + " traste " + fret);
            }
        }
    }

    @Test
    void hasNoNoteOffTheNeck() {
        FretboardView view = sized(new FretboardView());
        view.show(Tuning.standard(), Beat.rest(Duration.quarter()));

        assertEquals(Optional.empty(), view.noteAt(view.fretCenterX(5), 0));
        assertEquals(Optional.empty(), view.noteAt(view.fretCenterX(5), HEIGHT - 1));
    }

    @Test
    void hasNoNoteBeyondTheLastFret() {
        FretboardView view = sized(new FretboardView());
        view.show(Tuning.standard(), Beat.rest(Duration.quarter()));

        assertEquals(Optional.empty(), view.noteAt(WIDTH - 1, view.stringY(1)));
    }

    @Test
    void hasNoNoteBeforeTheOpenStrings() {
        FretboardView view = sized(new FretboardView());
        view.show(Tuning.standard(), Beat.rest(Duration.quarter()));

        assertEquals(Optional.empty(), view.noteAt(0, view.stringY(1)));
    }

    @Test
    void aBassStopsAtItsFourthString() {
        FretboardView bass = sized(new FretboardView());
        bass.show(Tuning.standardBass(), Beat.rest(Duration.quarter()));

        int lastString = bass.stringY(4);
        int aWholeStringFurtherDown = lastString + (lastString - bass.stringY(3));

        assertEquals(Optional.of(new Note(4, 3)), bass.noteAt(bass.fretCenterX(3), lastString));
        assertEquals(Optional.empty(), bass.noteAt(bass.fretCenterX(3), aWholeStringFurtherDown));
    }

    private static FretboardView sized(FretboardView view) {
        view.setSize(WIDTH, HEIGHT);
        return view;
    }

    private static BufferedImage paint(FretboardView view) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        view.paint(g);
        g.dispose();
        return image;
    }

    private static boolean hasMarkNear(BufferedImage image, int x, int y) {
        Color mark = InstrumentColors.PRESSED;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int px = x + dx;
                int py = y + dy;
                if (px >= 0 && py >= 0 && px < image.getWidth() && py < image.getHeight()
                        && image.getRGB(px, py) == mark.getRGB()) {
                    return true;
                }
            }
        }
        return false;
    }
}
