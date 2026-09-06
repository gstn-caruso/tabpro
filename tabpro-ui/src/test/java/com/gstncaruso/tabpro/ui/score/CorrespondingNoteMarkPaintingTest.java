package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.gstncaruso.tabpro.ui.page.PageMetrics;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Manual, linea 769-770: "cuando el cursor esta sobre una nota, la nota que le corresponde en la
 * otra notacion queda rodeada de un rectangulo gris". {@link ScorePainter#paintCorrespondingNote}
 * ya hace exactamente esto desde el clon original (v0.6.0) -no hacia falta escribirlo-, pero no
 * tenia ni un test propio: exactamente el patron de bug que este repo viene arrastrando, donde se
 * declara el color, se llama al metodo, y nada verifica que el pixel correcto se pinte. Estos
 * tests comparan la partitura pintada con y sin cursor, pixel a pixel -nunca preguntan un metodo.
 */
class CorrespondingNoteMarkPaintingTest {

    private static final int WIDTH = 900;

    /**
     * Con una sola nota por compas (sin acorde de por medio), la marca tiene que aparecer justo
     * en el grado que le corresponde a ESA nota, no en un lugar fijo del pentagrama. Se prueba
     * con dos notas bien separadas -grave y aguda- para que un pintor que marcara siempre la
     * misma altura quede en evidencia.
     */
    @Test
    void theCorrespondingNoteMarkTracksWhicheverNoteTheCursorIsOn() {
        Note low = new Note(6, 0);
        Note high = new Note(1, 12);
        Score lowScore = scoreWith(completeMeasureOf(low));
        Score highScore = scoreWith(completeMeasureOf(high));

        int yLow = stepYOf(lowScore, low);
        int yHigh = stepYOf(highScore, high);
        assertNotEquals(yLow, yHigh, "el fixture no sirve si las dos notas caen en el mismo grado");

        Painted lowBare = paint(lowScore, new Cursor(-1, 0, 0, 6));
        Painted lowOnNote = paint(lowScore, new Cursor(0, 0, 0, 6));
        Painted highBare = paint(highScore, new Cursor(-1, 0, 0, 1));
        Painted highOnNote = paint(highScore, new Cursor(0, 0, 0, 1));
        int x = lowBare.noteX();

        assertNotEquals(lowBare.pixelAt(x, yLow), lowOnNote.pixelAt(x, yLow),
                "la marca tiene que aparecer en el grado de la nota grave");
        assertNotEquals(highBare.pixelAt(x, yHigh), highOnNote.pixelAt(x, yHigh),
                "la marca tiene que aparecer en el grado de la nota aguda");
    }

    /**
     * El caso que justifica el feature entero: en un acorde de tres notas -tres alturas bien
     * distintas en el pentagrama- la marca tiene que caer solo en la cabeza de la cuerda del
     * cursor. Comparado nota por nota: si el pintor marcara siempre la misma cabeza, o el acorde
     * entero, alguna de estas seis comparaciones lo agarra.
     */
    @Test
    void inAChordTheMarkFallsOnlyOnTheNoteOfTheCursorsString() {
        Note lowString = new Note(6, 0);
        Note midString = new Note(4, 5);
        Note highString = new Note(1, 12);
        Score score = scoreWith(completeMeasureOf(Beat.of(Duration.quarter(), lowString, midString, highString)));

        int yLow = stepYOf(score, lowString);
        int yMid = stepYOf(score, midString);
        int yHigh = stepYOf(score, highString);
        assertNotEquals(yLow, yMid, "el fixture no sirve si dos notas caen en el mismo grado");
        assertNotEquals(yMid, yHigh, "el fixture no sirve si dos notas caen en el mismo grado");
        assertNotEquals(yLow, yHigh, "el fixture no sirve si dos notas caen en el mismo grado");

        Painted bare = paint(score, new Cursor(-1, 0, 0, 6));
        Painted onLowString = paint(score, new Cursor(0, 0, 0, 6));
        Painted onMidString = paint(score, new Cursor(0, 0, 0, 4));
        Painted onHighString = paint(score, new Cursor(0, 0, 0, 1));
        int x = bare.noteX();

        assertNotEquals(bare.pixelAt(x, yLow), onLowString.pixelAt(x, yLow), "falta la marca en la cuerda del cursor");
        assertEquals(bare.pixelAt(x, yMid), onLowString.pixelAt(x, yMid), "no puede aparecer marca en otra cuerda del acorde");
        assertEquals(bare.pixelAt(x, yHigh), onLowString.pixelAt(x, yHigh), "no puede aparecer marca en otra cuerda del acorde");

        assertEquals(bare.pixelAt(x, yLow), onMidString.pixelAt(x, yLow), "no puede aparecer marca en otra cuerda del acorde");
        assertNotEquals(bare.pixelAt(x, yMid), onMidString.pixelAt(x, yMid), "falta la marca en la cuerda del cursor");
        assertEquals(bare.pixelAt(x, yHigh), onMidString.pixelAt(x, yHigh), "no puede aparecer marca en otra cuerda del acorde");

        assertEquals(bare.pixelAt(x, yLow), onHighString.pixelAt(x, yLow), "no puede aparecer marca en otra cuerda del acorde");
        assertEquals(bare.pixelAt(x, yMid), onHighString.pixelAt(x, yMid), "no puede aparecer marca en otra cuerda del acorde");
        assertNotEquals(bare.pixelAt(x, yHigh), onHighString.pixelAt(x, yHigh), "falta la marca en la cuerda del cursor");
    }

    /**
     * Sin nota en la cuerda del cursor -silencio- no hay nada que rodear en la otra notacion, asi
     * que no puede aparecer ninguna marca en toda la franja del pentagrama.
     */
    @Test
    void aBeatWithoutANoteOnTheCursorsStringShowsNoCorrespondingMark() {
        Score score = scoreWith(new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()))));

        Painted bare = paint(score, new Cursor(-1, 0, 0, 3));
        Painted onRest = paint(score, new Cursor(0, 0, 0, 3));

        Rectangle beat = bare.layout.beatBounds(0, 0, 0);
        int top = bare.layout.staffTop(0, 0);
        int bottom = bare.layout.tabTop(0, 0);
        // Arranca en beat.x + 1, no en beat.x: ahi vive la linea vertical del cursor, que cambia
        // con o sin cursor por otro motivo (existe o no) y no tiene nada que ver con esta marca.
        assertTrue(
                sameInArea(bare, onRest, new Rectangle(beat.x + 1, top, beat.width - 2, bottom - top)),
                "sin nota en la cuerda del cursor no puede aparecer ninguna marca gris");
    }

    /**
     * ScoreColors.CORRESPONDING_NOTE esta en la tabla ON_PAPER (como LABEL, STAFF_LINE o
     * VOICE_INACTIVE): en el Modo Pagina tiene que salir invertido -gris oscuro sobre hoja clara-
     * y no el mismo gris claro con que se ve sobre el fondo oscuro de la pantalla, que ahi se
     * leeria destenido. Ver PageScorePainterTest.thePlayingLineIsTheSameGreenOnPaperAsOnScreen,
     * que hace la comprobacion analoga para un color que -a diferencia de este- no se invierte.
     */
    @Test
    void theCorrespondingNoteMarkReachesPageModeMirroredNotAsTheRawScreenColor() {
        Note note = new Note(1, 12);
        Score score = scoreWith(completeMeasureOf(note));
        ScoreViewport viewport = ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), WIDTH)
                .withPageSetup(PageSetup.defaults());

        BufferedImage bare = musicOf(renderPage(score, viewport, new Cursor(-1, 0, 0, 1)));
        BufferedImage onNote = musicOf(renderPage(score, viewport, new Cursor(0, 0, 0, 1)));

        ScoreLayout layout = PageScorePainter.layoutFor(score, viewport);
        double scale = PageMetrics.of(PageSetup.defaults()).scoreScale();
        int shiftUp = layout.systemTop(layout.systemOf(0));
        Rectangle beat = layout.beatBounds(0, 0, 0);
        int targetX = (int) Math.round((beat.x + beat.width / 2.0) * scale);
        int noteStep = StaffPosition.of(score.track(0).tuning().pitchOf(note), Clef.TREBLE).step();
        int targetY = (int) Math.round((layout.stepY(0, 0, noteStep) - shiftUp) * scale);

        int[] diff = firstDifferingPixelNear(bare, onNote, targetX, targetY, 4);
        assertNotNull(diff, "la marca tiene que llegar tambien a la hoja impresa, cerca de la nota");
        int base = bare.getRGB(diff[0], diff[1]);
        int actual = onNote.getRGB(diff[0], diff[1]);

        assertEquals(blended(base, ScoreColors.onPaper(ScoreColors.CORRESPONDING_NOTE)), actual,
                "en la hoja tiene que quedar el gris invertido de ON_PAPER");
        assertNotEquals(blended(base, ScoreColors.CORRESPONDING_NOTE), actual,
                "el gris crudo de pantalla, sin invertir, se leeria mal sobre el papel claro");
    }

    /**
     * El bug de verdad: bajo 8va/8vb/15ma/15mb, StaffPainter escribe la cabeza siete (o catorce)
     * grados corrida -{@link StaffPainter#positionOf} aplica {@code octaveMark.staffStepShift()}-
     * pero paintCorrespondingNote calculaba la posicion sin ese corrimiento. La marca quedaba
     * senalando pentagrama vacio (o la linea de otra nota) mientras la cabeza real estaba siete
     * grados mas arriba: una marca que apunta a la nota equivocada es peor que no tener marca.
     */
    @Test
    void theCorrespondingNoteMarkFollowsTheNoteWhenAnOctaveMarkMovesItsHead() {
        Note note = new Note(1, 0);
        Measure measure = new Measure(
                TimeSignature.fourFour(),
                MeasureAttributes.plain().withOctaveMark(OctaveMark.OTTAVA_ALTA),
                List.of(new Voice(List.of(
                        Beat.of(Duration.quarter(), note),
                        Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()))),
                        Voice.unused()));
        Score score = scoreWith(measure);

        StaffPosition unshiftedPosition = StaffPosition.of(score.track(0).tuning().pitchOf(note), Clef.TREBLE);
        int unshiftedStep = unshiftedPosition.step();
        int shiftedStep = unshiftedPosition.shiftedBySteps(OctaveMark.OTTAVA_ALTA.staffStepShift()).step();
        assertNotEquals(unshiftedStep, shiftedStep, "el fixture no sirve si 8va no mueve la cabeza");

        Painted bare = paint(score, new Cursor(-1, 0, 0, 1));
        Painted onNote = paint(score, new Cursor(0, 0, 0, 1));
        int x = bare.noteX();
        int yWhereTheHeadActuallyIs = bare.layout.stepY(0, 0, shiftedStep);
        int yWhereTheHeadWouldBeWithoutTheMark = bare.layout.stepY(0, 0, unshiftedStep);

        assertNotEquals(
                bare.pixelAt(x, yWhereTheHeadActuallyIs), onNote.pixelAt(x, yWhereTheHeadActuallyIs),
                "8va corre la cabeza siete grados: la marca tiene que seguirla hasta ahi");
        assertEquals(
                bare.pixelAt(x, yWhereTheHeadWouldBeWithoutTheMark), onNote.pixelAt(x, yWhereTheHeadWouldBeWithoutTheMark),
                "sin el corrimiento la marca queda senalando pentagrama vacio");
    }

    private static int stepYOf(Score score, Note note) {
        StaffPosition position = StaffPosition.of(score.track(0).tuning().pitchOf(note), Clef.TREBLE);
        return ScoreLayout.of(score, WIDTH).stepY(0, 0, position.step());
    }

    /** Cuatro negras -la nota y tres silencios- completan el compas de 4/4: uno incompleto se
     * pinta con un tinte de aviso que contaminaria la comparacion de pixeles. */
    private static Measure completeMeasureOf(Note note) {
        return completeMeasureOf(Beat.of(Duration.quarter(), note));
    }

    private static Measure completeMeasureOf(Beat firstBeat) {
        return new Measure(TimeSignature.fourFour(), List.of(
                firstBeat, Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
    }

    private static Score scoreWith(Measure measure) {
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("", 120, List.of(track));
    }

    private static Painted paint(Score score, Cursor cursor) {
        ScoreLayout layout = ScoreLayout.of(score, WIDTH);
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, WIDTH, layout.totalHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ScorePainter.paint(g, layout, score, cursor, Playhead.silent());
        g.dispose();
        return new Painted(image, layout);
    }

    private static BufferedImage renderPage(Score score, ScoreViewport viewport, Cursor cursor) {
        Dimension size = PageScorePainter.canvasSize(score, viewport);
        BufferedImage image = new BufferedImage(
                Math.max(1, size.width), Math.max(1, size.height), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        PageScorePainter.paint(g, score, cursor, Playhead.silent(), Optional.empty(), viewport);
        g.dispose();
        return image;
    }

    private static BufferedImage musicOf(BufferedImage sheet) {
        PageMetrics paper = PageMetrics.of(PageSetup.defaults());
        return sheet.getSubimage(paper.contentLeft(), paper.contentTop(), paper.contentWidth(), paper.contentHeight());
    }

    private static boolean sameInArea(Painted one, Painted other, Rectangle area) {
        for (int x = area.x; x < area.x + area.width; x++) {
            for (int y = area.y; y < area.y + area.height; y++) {
                if (one.image.getRGB(x, y) != other.image.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Busca, en un cuadrado alrededor de (x,y), el primer pixel donde difieren dos imagenes. */
    private static int[] firstDifferingPixelNear(BufferedImage one, BufferedImage other, int x, int y, int radius) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int px = x + dx;
                int py = y + dy;
                if (px >= 0 && py >= 0 && px < one.getWidth() && py < one.getHeight()
                        && one.getRGB(px, py) != other.getRGB(px, py)) {
                    return new int[] {px, py};
                }
            }
        }
        return null;
    }

    /** Como queda un pixel de fondo {@code baseRGB} despues de pintarle encima {@code overlay}:
     * deja que el propio AWT haga la mezcla alfa, en vez de repetir su formula a mano. */
    private static int blended(int baseRGB, Color overlay) {
        BufferedImage tiny = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        tiny.setRGB(0, 0, baseRGB);
        Graphics2D g = tiny.createGraphics();
        g.setColor(overlay);
        g.fillRect(0, 0, 1, 1);
        g.dispose();
        return tiny.getRGB(0, 0);
    }

    private record Painted(BufferedImage image, ScoreLayout layout) {

        int noteX() {
            Rectangle beat = layout.beatBounds(0, 0, 0);
            return beat.x + beat.width / 2;
        }

        int pixelAt(int x, int y) {
            return image.getRGB(x, y);
        }
    }
}
