package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 8va/8vb/15ma/15mb del manual: cambian donde se escribe la nota en el pentagrama, nunca lo que
 * se toca. El test que importa no es que la marca haya quedado guardada, sino las dos mitades
 * juntas: la nota se escribe en otro lugar del pentagrama Y la tablatura -lo que de verdad se
 * toca- se dibuja exactamente igual que sin la marca.
 */
class OctaveMarkPaintingTest {

    private static final int WIDTH = 900;
    // La prima al aire cae cerca del espacio superior del pentagrama (sin lineas adicionales);
    // 8va la escribe justo en la linea de abajo, asi el corrimiento no arrastra lineas
    // adicionales hasta pisar la tablatura y contaminar la comparacion pixel a pixel de abajo.
    private static final Note NOTE = new Note(1, 0);

    @Test
    void ottavaAltaWritesTheNoteSevenStepsLowerOnTheStaff() {
        assertWritesAt(OctaveMark.OTTAVA_ALTA, -7);
    }

    @Test
    void ottavaBassaWritesTheNoteSevenStepsHigherOnTheStaff() {
        assertWritesAt(OctaveMark.OTTAVA_BASSA, 7);
    }

    @Test
    void quindicesimaAltaWritesTheNoteFourteenStepsLowerOnTheStaff() {
        assertWritesAt(OctaveMark.QUINDICESIMA_ALTA, -14);
    }

    @Test
    void quindicesimaBassaWritesTheNoteFourteenStepsHigherOnTheStaff() {
        assertWritesAt(OctaveMark.QUINDICESIMA_BASSA, 14);
    }

    private void assertWritesAt(OctaveMark octaveMark, int expectedStepDelta) {
        Painted plain = paint(OctaveMark.NONE);
        Painted marked = paint(octaveMark);
        int x = plain.noteX();
        int plainStep = StaffPosition.of(Tuning.standard().pitchOf(NOTE), Clef.TREBLE).step();

        assertTrue(plain.hasInkNear(x, plain.layout.stepY(0, 0, plainStep), 2),
                "sin marca la nota tiene que estar en su lugar de siempre");
        assertFalse(marked.hasInkNear(x, marked.layout.stepY(0, 0, plainStep), 2),
                octaveMark.label() + " tiene que sacar la nota de donde estaba");
        assertTrue(marked.hasInkNear(x, marked.layout.stepY(0, 0, plainStep + expectedStepDelta), 2),
                octaveMark.label() + " tiene que escribir la nota " + Math.abs(expectedStepDelta)
                        + " grados " + (expectedStepDelta < 0 ? "mas abajo" : "mas arriba"));
    }

    @Test
    void anOctaveMarkNeverMovesTheNoteHorizontally() {
        Painted plain = paint(OctaveMark.NONE);
        Painted marked = paint(OctaveMark.OTTAVA_ALTA);

        assertEquals(plain.noteX(), marked.noteX());
    }

    /** La mitad que mas importa: lo que de verdad se toca -la tablatura- no se mueve un pixel. */
    @Test
    void anOctaveMarkNeverChangesWhatTheTablatureShows() {
        Painted plain = paint(OctaveMark.NONE);
        Painted marked = paint(OctaveMark.OTTAVA_ALTA);

        assertTrue(plain.tabAreaLooksLike(marked),
                "la tablatura tiene que quedar identica: la marca solo cambia el pentagrama");
    }

    /**
     * El extremo derecho del compas -donde llega la linea de puntos, pero nunca la plica ni las
     * lineas adicionales de la nota, que quedan centradas sobre su propia cabeza- es el unico
     * lugar limpio para preguntar si la marca dibujo algo, sin que un pentagrama ya ocupado por
     * la clave (a la izquierda) o por la nota (al medio) de un falso positivo.
     */
    @Test
    void withoutAMarkNothingReachesTheEndOfTheMeasureAboveOrBelowTheStaff() {
        Painted plain = paint(OctaveMark.NONE);

        assertFalse(plain.hasInkAboveTheStaffNear(plain.rightEdge()));
        assertFalse(plain.hasInkBelowTheStaffNear(plain.rightEdge()));
    }

    @Test
    void ottavaAltaDrawsAboveTheStaffWithADottedLineReachingTheEndOfTheMeasure() {
        Painted marked = paint(OctaveMark.OTTAVA_ALTA);

        assertTrue(marked.hasInkAboveTheStaffNear(marked.rightEdge()), "8va se dibuja arriba del pentagrama");
        assertFalse(marked.hasInkBelowTheStaffNear(marked.rightEdge()));
    }

    @Test
    void ottavaBassaDrawsBelowTheStaffWithADottedLineReachingTheEndOfTheMeasure() {
        Painted marked = paint(OctaveMark.OTTAVA_BASSA);

        assertTrue(marked.hasInkBelowTheStaffNear(marked.rightEdge()), "8vb se dibuja abajo del pentagrama");
        assertFalse(marked.hasInkAboveTheStaffNear(marked.rightEdge()));
    }

    private static Painted paint(OctaveMark octaveMark) {
        // Cuatro negras -la nota y tres silencios- completan el compas de 4/4: uno incompleto
        // se pinta con un tinte de aviso que taparia justo la franja que estos tests miran.
        Measure measure = new Measure(
                TimeSignature.fourFour(),
                MeasureAttributes.plain().withOctaveMark(octaveMark),
                List.of(new Voice(List.of(
                        Beat.of(Duration.quarter(), NOTE),
                        Beat.rest(Duration.quarter()),
                        Beat.rest(Duration.quarter()),
                        Beat.rest(Duration.quarter()))), Voice.unused()));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));

        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, WIDTH, layout.totalHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        // Cursor fuera de la partitura: sin el, ScorePainter resalta con un rectangulo gris la
        // nota correspondiente en la otra notacion en la posicion SIN corrimiento -logica de
        // ScorePainter, fuera del alcance de este cambio- y contamina la comparacion de pixeles.
        ScorePainter.paint(g, layout, score, new Cursor(-1, 0, 0, 1), Playhead.silent());
        g.dispose();
        return new Painted(image, layout);
    }

    private record Painted(BufferedImage image, ScoreLayout layout) {

        int noteX() {
            Rectangle beat = layout.beatBounds(0, 0, 0);
            return beat.x + beat.width / 2;
        }

        int rightEdge() {
            return layout.measureX(0) + layout.measureWidth(0) - 3;
        }

        boolean hasInkNear(int x, int y, int radius) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (isInside(x + dx, y + dy)
                            && image.getRGB(x + dx, y + dy) != ScoreColors.BACKGROUND.getRGB()) {
                        return true;
                    }
                }
            }
            return false;
        }

        /** Deja 3px de aire pegados al pentagrama: ahi puede sangrar el trazo de la linea
         * superior del pentagrama, sin antialiasing de por medio, y no tiene nada que ver con
         * la marca de octava que este test busca. */
        private static final int STAFF_BLEED_MARGIN = 3;

        boolean hasInkAboveTheStaffNear(int x) {
            int top = layout.trackTop(0, 0) + ScoreLayout.TRACK_LABEL_HEIGHT;
            int bottom = layout.staffTop(0, 0) - STAFF_BLEED_MARGIN;
            return hasInkIn(new Rectangle(x - 2, top, 4, bottom - top));
        }

        boolean hasInkBelowTheStaffNear(int x) {
            int top = layout.staffBottom(0, 0) + STAFF_BLEED_MARGIN;
            int bottom = layout.tabTop(0, 0) - 1;
            return hasInkIn(new Rectangle(x - 2, top, 4, bottom - top));
        }

        boolean tabAreaLooksLike(Painted other) {
            Rectangle area = tabArea();
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    if (isInside(x, y) && other.isInside(x, y)
                            && image.getRGB(x, y) != other.image.getRGB(x, y)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private Rectangle tabArea() {
            return new Rectangle(
                    layout.measureX(0), layout.tabTop(0, 0),
                    layout.measureWidth(0), layout.tabBottom(0, 0) - layout.tabTop(0, 0));
        }

        private boolean hasInkIn(Rectangle area) {
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    if (isInside(x, y) && image.getRGB(x, y) != ScoreColors.BACKGROUND.getRGB()) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isInside(int x, int y) {
            return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
        }
    }
}
