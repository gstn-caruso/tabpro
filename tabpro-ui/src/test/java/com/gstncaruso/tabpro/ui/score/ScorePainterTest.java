package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScorePainterTest {

    private static final int WIDTH = 900;

    @Test
    void paintsTheBackground() {
        Painted painted = paint(Score.blank(), new Cursor(0, 0, 0, 1), Playhead.silent());

        assertEquals(ScoreColors.BACKGROUND.getRGB(), painted.image().getRGB(0, 0));
    }

    @Test
    void drawsTheFiveStaffLinesAboveTheTablature() {
        Painted painted = paint(Score.blank(), new Cursor(0, 0, 0, 1), Playhead.silent());
        int x = painted.layout().measureX(0) + painted.layout().measureWidth(0) - 4;

        for (int line = 0; line <= 4; line++) {
            int y = painted.layout().staffLineY(0, 0, line);
            assertTrue(painted.hasInkNear(x, y, 1), "falta la linea " + line + " del pentagrama");
        }
    }

    @Test
    void drawsOneTablatureLinePerString() {
        Painted painted = paint(Score.blank(), new Cursor(0, 0, 0, 1), Playhead.silent());
        int x = painted.layout().measureX(0) + painted.layout().measureWidth(0) - 4;

        for (int string = 1; string <= 6; string++) {
            int y = painted.layout().stringY(0, 0, string);
            assertTrue(painted.hasInkNear(x, y, 1), "falta la cuerda " + string);
        }
    }

    @Test
    void writesANoteOnTheStaffWhereItsPitchBelongs() {
        Score score = scoreWith(measureOf(Beat.of(Duration.quarter(), new Note(6, 0))));
        Painted painted = paint(score, new Cursor(0, 0, 0, 1), Playhead.silent());

        StaffPosition position = StaffPosition.of(
                score.track(0).tuning().pitchOf(new Note(6, 0)), Clef.TREBLE);
        Rectangle beat = painted.layout().beatBounds(0, 0, 0);
        int y = painted.layout().stepY(0, 0, position.step());

        assertTrue(painted.hasInkNear(beat.x + beat.width / 2, y, 4), "falta la cabeza de la nota");
    }

    @Test
    void aBassTrackIsWrittenLowerThanTheSameSoundingPitchOnAGuitar() {
        Note openA = new Note(3, 0);
        int guitarStep = StaffPosition.of(Tuning.standard().pitchOf(new Note(3, 2)), Clef.TREBLE).step();
        int bassStep = StaffPosition.of(Tuning.standardBass().pitchOf(openA), Clef.BASS).step();

        assertFalse(guitarStep == bassStep, "las dos claves no pueden dar el mismo grado");
        assertDoesNotThrow(() -> paint(
                new Score("", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))),
                new Cursor(0, 0, 0, 1),
                Playhead.silent()));
    }

    @Test
    void outlinesTheCellUnderTheCursor() {
        Cursor cursor = new Cursor(0, 0, 0, 3);
        Painted painted = paint(Score.blank(), cursor, Playhead.silent());

        Rectangle beat = painted.layout().beatBounds(0, 0, 0);
        int y = painted.layout().stringY(0, 0, 3) - ScoreLayout.STRING_SPACING / 2 + 1;

        assertEquals(ScoreColors.CURSOR.getRGB(), painted.image().getRGB(beat.x + beat.width / 2, y));
    }

    @Test
    void tintsTheBeatThatIsSounding() {
        Score score = new Score("", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo")));
        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(1, 0, 0));
        Painted painted = paint(score, new Cursor(0, 0, 0, 1), playhead);

        Rectangle beat = painted.layout().beatBounds(1, 0, 0);
        int y = painted.layout().staffTop(1, 0) + 2;

        assertFalse(
                painted.image().getRGB(beat.x + beat.width / 2, y) == ScoreColors.BACKGROUND.getRGB(),
                "el beat que suena tiene que quedar resaltado");
    }

    @Test
    void paintsEveryTrackInItsOwnBand() {
        Score score = new Score("", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo")));
        Painted painted = paint(score, new Cursor(0, 0, 0, 1), Playhead.silent());
        int x = painted.layout().measureX(0) + painted.layout().measureWidth(0) - 4;

        assertTrue(painted.hasInkNear(x, painted.layout().staffLineY(0, 0, 0), 1));
        assertTrue(painted.hasInkNear(x, painted.layout().staffLineY(1, 0, 0), 1));
        assertTrue(painted.layout().staffTop(1, 0) > painted.layout().tabBottom(0, 0));
    }

    @Test
    void survivesEveryFigureRestChordAndBeam() {
        Measure crowded = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(new Duration(NoteValue.SIXTEENTH, false), new Note(1, 12)),
                Beat.of(new Duration(NoteValue.SIXTEENTH, false), new Note(1, 10)),
                Beat.of(new Duration(NoteValue.THIRTY_SECOND, false), new Note(2, 3)),
                Beat.rest(new Duration(NoteValue.EIGHTH, true)),
                Beat.of(new Duration(NoteValue.HALF, false), new Note(6, 1), new Note(5, 3)),
                Beat.rest(new Duration(NoteValue.WHOLE, false)),
                Beat.of(new Duration(NoteValue.QUARTER, true), new Note(4, 7))));

        assertDoesNotThrow(() -> paint(scoreWith(crowded), new Cursor(0, 0, 0, 1), Playhead.silent()));
    }

    @Test
    void survivesEverySymbolTheManualListsForTheTablature() {
        Note ghost = new Note(1, 5).toggling(
                com.gstncaruso.tabpro.core.model.effects.Ornament.GHOST);
        Note dead = new Note(2, 0).toggling(com.gstncaruso.tabpro.core.model.effects.Ornament.DEAD);
        Note tied = new Note(3, 3).toggling(com.gstncaruso.tabpro.core.model.effects.Ornament.HAMMER_ON_PULL_OFF);
        Note tiedTo = Note.tiedOn(3).withFret(5);
        Note bent = new Note(4, 7).withBend(
                com.gstncaruso.tabpro.core.model.effects.Bend.of(
                        com.gstncaruso.tabpro.core.model.effects.BendType.BEND, 4));
        Note slid = new Note(5, 2).withSlide(com.gstncaruso.tabpro.core.model.effects.SlideType.OUT_UPWARDS);
        Note harmonic = new Note(6, 12).withHarmonic(com.gstncaruso.tabpro.core.model.effects.HarmonicType.NATURAL);

        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), ghost, dead)
                        .withEffects(com.gstncaruso.tabpro.core.model.effects.BeatEffects.none()
                                .withTapping(true)
                                .withStroke(com.gstncaruso.tabpro.core.model.effects.Stroke.of(
                                        com.gstncaruso.tabpro.core.model.effects.StrokeDirection.DOWN))
                                .withText("rit.")),
                Beat.of(Duration.quarter(), tied),
                Beat.of(Duration.quarter(), tiedTo, bent),
                Beat.of(Duration.quarter(), slid, harmonic)));

        assertDoesNotThrow(() -> paint(scoreWith(measure), new Cursor(0, 0, 0, 1), Playhead.silent()));
    }

    @Test
    void survivesTwoVoicesAKeySignatureAndATuplet() {
        Measure leadOnly = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 5)),
                Beat.of(Duration.quarter(), new Note(1, 7)),
                Beat.of(Duration.quarter(), new Note(1, 8)),
                Beat.of(Duration.quarter(), new Note(1, 5))));
        Measure withBass = leadOnly.withVoice(
                com.gstncaruso.tabpro.core.model.VoicePart.BASS,
                new com.gstncaruso.tabpro.core.model.Voice(List.of(
                        Beat.of(Duration.quarter(), new Note(6, 0)),
                        Beat.of(Duration.quarter(), new Note(6, 0)),
                        Beat.of(Duration.quarter(), new Note(6, 3)),
                        Beat.of(Duration.quarter(), new Note(6, 0)))));
        Duration eighthTriplet = new Duration(NoteValue.EIGHTH, false)
                .in(com.gstncaruso.tabpro.core.model.Tuplet.of(3));
        Measure withTuplet = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(eighthTriplet, new Note(1, 0)),
                Beat.of(eighthTriplet, new Note(1, 2)),
                Beat.of(eighthTriplet, new Note(1, 4)),
                Beat.of(Duration.quarter(), new Note(1, 0))))
                .mappingAttributes(attrs -> attrs.withKeySignature(
                        new com.gstncaruso.tabpro.core.model.bars.KeySignature(
                                2, com.gstncaruso.tabpro.core.model.bars.Mode.MAJOR)));

        Score score = scoreWith(withBass, withTuplet);

        assertDoesNotThrow(() -> paint(score, new Cursor(0, 0, 0, 1), Playhead.silent()));
    }

    @Test
    void survivesRepeatsAlternateEndingsDirectionsAndMarkers() {
        Measure opens = measureOf(Beat.of(Duration.quarter(), new Note(1, 0)))
                .mappingAttributes(attrs -> attrs.withRepeatOpen(true)
                        .withMarker(com.gstncaruso.tabpro.core.model.bars.Marker.named("Intro")));
        Measure firstEnding = measureOf(Beat.of(Duration.quarter(), new Note(1, 1)))
                .mappingAttributes(attrs -> attrs.withAlternateEndings(List.of(1)));
        Measure closes = measureOf(Beat.of(Duration.quarter(), new Note(1, 2)))
                .mappingAttributes(attrs -> attrs.withRepeatCount(1).withDoubleBar(true)
                        .withSymbol(com.gstncaruso.tabpro.core.model.bars.DirectionSymbol.CODA)
                        .withJump(com.gstncaruso.tabpro.core.model.bars.DirectionJump.DA_CAPO_AL_CODA));

        Score score = scoreWith(opens, firstEnding, closes);

        assertDoesNotThrow(() -> paint(score, new Cursor(0, 0, 0, 1), Playhead.silent()));
    }

    @Test
    void survivesLyricsAndAChordDiagram() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)).withEffects(
                        com.gstncaruso.tabpro.core.model.effects.BeatEffects.none().withChord(
                                com.gstncaruso.tabpro.core.model.chords.ChordDiagram.named(
                                        "Do", List.of(-1, 3, 2, 0, 1, 0)))),
                Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3))));
        Score score = scoreWith(measure).withLyrics(
                com.gstncaruso.tabpro.core.model.Lyrics.none().onTrack(0)
                        .withLine(0, com.gstncaruso.tabpro.core.model.LyricLine.empty()
                                .startingAt(1).saying("La vi-da es un sue-no")));

        assertDoesNotThrow(() -> paint(score, new Cursor(0, 0, 0, 1), Playhead.silent()));
    }

    @Test
    void survivesAPercussionTrackAndAMultiBeatSelection() {
        Track kit = Track.percussion("Bateria").withMeasures(List.of(
                new Measure(TimeSignature.fourFour(), List.of(
                        Beat.of(Duration.quarter(), new Note(1, 42)),
                        Beat.of(Duration.quarter(), new Note(2, 38)),
                        Beat.of(Duration.quarter(), new Note(6, 36)),
                        Beat.of(Duration.quarter(), new Note(3, 56))))));
        Score score = new Score("", 120, List.of(kit));
        com.gstncaruso.tabpro.core.editing.Selection selection =
                com.gstncaruso.tabpro.core.editing.Selection.ofMeasures(0, 0, 0);
        ScoreLayout layout = ScoreLayout.of(score, WIDTH);
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        assertDoesNotThrow(() -> ScorePainter.paint(
                g, layout, score, new Cursor(0, 0, 0, 1), Playhead.silent(), java.util.Optional.of(selection)));
        g.dispose();
    }

    /**
     * El manual: al seleccionar compases enteros (Ctrl+clic) la seleccion tiene que abarcar el
     * compas completo, no solo los beats que tiene la voz principal. Un compas siempre deja un
     * margen (MEASURE_LEFT_PADDING/MEASURE_RIGHT_PADDING) que ningun beat pisa; si la seleccion
     * de compas entero pinta ese margen tambien, es que esta usando el ancho del compas y no el
     * de sus beats.
     */
    @Test
    void aWholeMeasureSelectionPaintsTheFullMeasureIncludingItsMargin() {
        Measure full = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3))));
        Score score = scoreWith(full);
        com.gstncaruso.tabpro.core.editing.Selection selection =
                com.gstncaruso.tabpro.core.editing.Selection.ofMeasures(0, 0, 0);
        ScoreLayout layout = ScoreLayout.of(score, WIDTH);
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        ScorePainter.paint(
                g, layout, score, new Cursor(0, 0, 0, 1), Playhead.silent(), java.util.Optional.of(selection));
        g.dispose();

        Rectangle measureBounds = layout.measureBounds(0, 0);
        int marginX = measureBounds.x + 2;
        int y = measureBounds.y + measureBounds.height / 2;

        assertNotEquals(ScoreColors.BACKGROUND.getRGB(), image.getRGB(marginX, y),
                "el margen izquierdo del compas tiene que quedar pintado tambien");
    }

    @Test
    void aTrackThatIsNotShownIsNotDrawnAtAll() {
        Track guitar = Track.standardGuitar("Guitarra");
        Score two = new Score("", 120, List.of(guitar, guitar));
        Score one = new Score("", 120, List.of(guitar));

        Painted withoutTheFirst = paint(two, new Cursor(1, 0, 0, 1), Playhead.silent(),
                VisibleTracks.all().withActiveTrack(1).withTrackShown(0, false));
        Painted alone = paint(one, new Cursor(0, 0, 0, 1), Playhead.silent(), VisibleTracks.all());

        assertTrue(withoutTheFirst.looksLike(alone),
                "apagar una pista tiene que dar la misma hoja que no tenerla");
    }

    @Test
    void theBarStructureIsDrawnEvenWhenTheFirstTrackIsNotShown() {
        Track guitar = Track.standardGuitar("Guitarra");
        Measure repeated = Measure.empty(TimeSignature.fourFour(), Duration.quarter())
                .withAttributes(Measure.empty(TimeSignature.fourFour(), Duration.quarter())
                        .attributes().withRepeatOpen(true));
        Track second = new Track("Bajo", guitar.tuning(), guitar.channel(), List.of(repeated));
        Score score = new Score("", 120, List.of(
                new Track("Guitarra", guitar.tuning(), guitar.channel(), List.of(repeated)), second));

        Painted painted = paint(score, new Cursor(1, 0, 0, 1), Playhead.silent(),
                VisibleTracks.all().withActiveTrack(1).withTrackShown(0, false));

        int x = painted.layout().measureX(0) + 2;
        int y = (painted.layout().staffTop(1, 0) + painted.layout().tabBottom(1, 0)) / 2;
        assertTrue(painted.hasInkNear(x, y, 3), "falta la barra de repeticion de la pista que si se ve");
    }

    @Test
    void unFadeInSeAnunciaSobreLaTablatura() {
        Beat conFade = Beat.of(Duration.quarter(), new Note(1, 5))
                .withEffects(BeatEffects.none().withFadeIn(true));
        Beat sinFade = Beat.of(Duration.quarter(), new Note(1, 5));

        assertTrue(
                inkAboveTheTablature(conFade) > inkAboveTheTablature(sinFade),
                "el fade in tiene que dejar su etiqueta arriba de la tablatura");
    }

    @Test
    void laPalancaSeDibujaBajoLaTablatura() {
        Bend dive = new Bend(BendType.BEND_RELEASE, List.of(
                BendPoint.at(0, 0), BendPoint.at(30, -4), BendPoint.at(BendPoint.LAST_POSITION, 0)));
        Beat conPalanca = Beat.of(Duration.quarter(), new Note(1, 5))
                .withEffects(BeatEffects.none().withTremoloBar(dive));
        Beat sinPalanca = Beat.of(Duration.quarter(), new Note(1, 5));

        assertTrue(
                inkUnderTheTablature(conPalanca) > inkUnderTheTablature(sinPalanca),
                "la palanca suena pero no se ve: falta su curva bajo la tablatura");
    }

    @Test
    void elWahWahSeAnunciaSobreLaTablatura() {
        Beat conWah = Beat.of(Duration.quarter(), new Note(1, 5))
                .withEffects(BeatEffects.none().withWah(Wah.OPEN));
        Beat sinWah = Beat.of(Duration.quarter(), new Note(1, 5));

        assertTrue(
                inkAboveTheTablature(conWah) > inkAboveTheTablature(sinWah),
                "el pedal de wah-wah tiene que quedar anotado arriba de la tablatura");
    }

    /**
     * La transicion de la nota de adorno se elige en el dialogo y se guarda, pero
     * la hoja quedaba igual con cualquiera de las cuatro: una notita suelta y
     * desconectada. Cada una tiene que dejar su propia marca hasta la nota.
     */
    @Test
    void laTransicionDeLaNotaDeAdornoSeDibujaHastaLaNota() {
        int sinTransicion = inkBetweenTheGraceNoteAndTheNote(GraceTransition.NONE);

        assertTrue(inkBetweenTheGraceNoteAndTheNote(GraceTransition.SLIDE) > sinTransicion, "falta el slide");
        assertTrue(inkBetweenTheGraceNoteAndTheNote(GraceTransition.BEND) > sinTransicion, "falta el bend");
        assertTrue(inkBetweenTheGraceNoteAndTheNote(GraceTransition.HAMMER) > sinTransicion, "falta el ligado");
    }

    private static int inkBetweenTheGraceNoteAndTheNote(GraceTransition transition) {
        Note note = new Note(3, 5).withEffects(NoteEffects.none().withGrace(new GraceNote(
                3, NoteValue.THIRTY_SECOND, Dynamic.defaultDynamic(), transition, false, false)));
        Painted painted = paint(
                scoreWith(measureOf(Beat.of(Duration.quarter(), note))),
                new Cursor(0, 0, 0, 6), Playhead.silent());

        Rectangle bounds = painted.layout().beatBounds(0, 0, 0);
        int y = painted.layout().stringY(0, 0, 3);
        int from = bounds.x + 2;
        int to = bounds.x + bounds.width / 2 - 8;
        return painted.inkIn(new Rectangle(from, y - 10, to - from, 9));
    }

    private static int inkUnderTheTablature(Beat beat) {
        Painted painted = paint(scoreWith(measureOf(beat)), new Cursor(0, 0, 0, 3), Playhead.silent());
        Rectangle bounds = painted.layout().beatBounds(0, 0, 0);
        int tabBottom = painted.layout().tabBottom(0, 0);
        return painted.inkIn(new Rectangle(bounds.x, tabBottom + 1, bounds.width, 22));
    }

    private static int inkAboveTheTablature(Beat beat) {
        Painted painted = paint(scoreWith(measureOf(beat)), new Cursor(0, 0, 0, 3), Playhead.silent());
        Rectangle bounds = painted.layout().beatBounds(0, 0, 0);
        int tabTop = painted.layout().tabTop(0, 0);
        return painted.inkIn(new Rectangle(bounds.x, tabTop - 34, bounds.width, 32));
    }

    private static Score scoreWith(Measure... measures) {
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measures));
        return new Score("", 120, List.of(track));
    }

    private static Measure measureOf(Beat... beats) {
        return new Measure(TimeSignature.fourFour(), List.of(beats));
    }

    private static Score scoreWith(Measure measure) {
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("", 120, List.of(track));
    }

    private static Painted paint(Score score, Cursor cursor, Playhead playhead) {
        return paint(score, cursor, playhead, VisibleTracks.all());
    }

    private static Painted paint(Score score, Cursor cursor, Playhead playhead, VisibleTracks visibleTracks) {
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, visibleTracks);
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, WIDTH, layout.totalHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ScorePainter.paint(g, layout, score, cursor, playhead);
        g.dispose();
        return new Painted(image, layout);
    }

    private record Painted(BufferedImage image, ScoreLayout layout) {

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

        /** Cuanta tinta hay en un rectangulo: sirve para comparar la misma hoja con y sin un efecto. */
        int inkIn(Rectangle area) {
            int ink = 0;
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    if (isInside(x, y) && image.getRGB(x, y) != ScoreColors.BACKGROUND.getRGB()) {
                        ink++;
                    }
                }
            }
            return ink;
        }

        boolean looksLike(Painted other) {
            if (image.getWidth() != other.image.getWidth() || image.getHeight() != other.image.getHeight()) {
                return false;
            }
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (image.getRGB(x, y) != other.image.getRGB(x, y)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isInside(int x, int y) {
            return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
        }
    }
}
