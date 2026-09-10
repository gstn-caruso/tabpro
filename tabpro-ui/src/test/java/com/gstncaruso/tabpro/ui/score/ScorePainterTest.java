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
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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

    /**
     * El manual pide el cursor de edicion como una linea vertical fina que cruza el pentagrama y
     * la tablatura -no como el recuadro de antes, que solo marcaba una cuerda-. Se ubica en el
     * arranque del beat actual, igual convencion que la linea de reproduccion.
     */
    @Test
    void theEditingCursorIsAThinRedLineAcrossTheStaffAndTheTablature() {
        Cursor cursor = new Cursor(0, 0, 0, 3);
        Painted painted = paint(Score.blank(), cursor, Playhead.silent());

        int x = painted.layout().beatBounds(0, 0, 0).x;
        int nearTheStaff = painted.layout().staffTop(0, 0) + 2;
        int nearTheTablature = painted.layout().tabBottom(0, 0) - 2;

        assertEquals(ScoreColors.CURSOR.getRGB(), painted.image().getRGB(x, nearTheStaff),
                "la linea del cursor tiene que cruzar el pentagrama");
        assertEquals(ScoreColors.CURSOR.getRGB(), painted.image().getRGB(x, nearTheTablature),
                "la linea del cursor tiene que cruzar tambien la tablatura");
    }

    @Test
    void theEditingCursorIsThinNotARectangleFillingTheBeat() {
        Cursor cursor = new Cursor(0, 0, 0, 3);
        Painted painted = paint(Score.blank(), cursor, Playhead.silent());

        Rectangle beat = painted.layout().beatBounds(0, 0, 0);
        int y = painted.layout().tabTop(0, 0) + 3;
        int farFromTheLine = beat.x + beat.width - 2;

        assertNotEquals(ScoreColors.CURSOR.getRGB(), painted.image().getRGB(farFromTheLine, y),
                "el cursor no puede tapar el beat entero como el recuadro de antes");
    }

    /**
     * La linea sola no dice en que cuerda esta parado el cursor -eso lo hacia bien el recuadro de
     * antes-, asi que a la altura de esa cuerda la marca tiene que ensancharse.
     */
    @Test
    void theEditingCursorStillShowsWhichStringItIsOn() {
        Cursor cursor = new Cursor(0, 0, 0, 3);
        Painted painted = paint(Score.blank(), cursor, Playhead.silent());

        int x = painted.layout().beatBounds(0, 0, 0).x;
        int onItsString = painted.layout().stringY(0, 0, 3);
        int onAnotherString = painted.layout().stringY(0, 0, 5);

        assertEquals(ScoreColors.CURSOR.getRGB(), painted.image().getRGB(x - 2, onItsString),
                "en su cuerda la marca tiene que ser mas ancha que la linea");
        assertNotEquals(ScoreColors.CURSOR.getRGB(), painted.image().getRGB(x - 2, onAnotherString),
                "en otra cuerda no tiene que aparecer esa marca ancha");
    }

    @Test
    void theEditingCursorLineCrossesTheOtherTracksInTheSystemToo() {
        Score score = new Score("", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo")));
        Cursor cursor = new Cursor(0, 0, 0, 3);
        Painted painted = paint(score, cursor, Playhead.silent());

        int x = painted.layout().beatBounds(0, 0, 0).x;
        int nearTheOtherTrack = painted.layout().trackTop(1, 0) + 5;

        assertTrue(
                nearTheOtherTrack < painted.layout().staffTop(1, 0),
                "el punto de control tiene que estar arriba de la pista de abajo, no adentro");
        assertTrue(painted.hasInkNear(x, nearTheOtherTrack, 0),
                "la linea del cursor tiene que cruzar tambien la pista que no se esta editando");
    }

    @Test
    void theEditingCursorIsDimmedOverTheOtherTracks() {
        Score score = new Score("", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo")));
        Cursor cursor = new Cursor(0, 0, 0, 3);
        Painted painted = paint(score, cursor, Playhead.silent());

        int x = painted.layout().beatBounds(0, 0, 0).x;
        int onTheOtherTrack = painted.layout().staffTop(1, 0) + 2;

        assertNotEquals(ScoreColors.CURSOR.getRGB(), painted.image().getRGB(x, onTheOtherTrack),
                "sobre la pista que no se edita el cursor tiene que quedar atenuado, no pleno");
    }

    @Test
    void theEditingCursorIsFullRedOnlyOverItsOwnTrack() {
        Score score = new Score("", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo")));
        Cursor cursor = new Cursor(0, 0, 0, 3);
        Painted painted = paint(score, cursor, Playhead.silent());

        int x = painted.layout().beatBounds(0, 0, 0).x;
        int onItsOwnTrack = painted.layout().staffTop(0, 0) + 2;

        assertEquals(ScoreColors.CURSOR.getRGB(), painted.image().getRGB(x, onItsOwnTrack),
                "sobre su propia pista el cursor tiene que seguir siendo el rojo pleno");
    }

    @Test
    void drawsAThinLineAtTheBeatThatIsSoundingInsteadOfAFilledBlock() {
        Score score = new Score("", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo")));
        Painted silent = paint(score, new Cursor(0, 0, 0, 1), Playhead.silent());
        Painted playing = paint(
                score, new Cursor(0, 0, 0, 1), Playhead.silent().advancedTo(new BeatPosition(1, 0, 0)));

        Rectangle beat = playing.layout().beatBounds(1, 0, 0);
        int lineX = beat.x;
        int elsewhereX = beat.x + beat.width - 2;
        int y = playing.layout().tabTop(1, 0) + 3;

        assertNotEquals(
                silent.image().getRGB(lineX, y), playing.image().getRGB(lineX, y),
                "la linea de reproduccion tiene que marcar donde arranca el beat que suena");
        assertEquals(
                silent.image().getRGB(elsewhereX, y), playing.image().getRGB(elsewhereX, y),
                "el resto del beat no puede quedar tapado por un bloque relleno como antes");
    }

    @Test
    void thePlayingLineCrossesTheWholeSystemNotJustTheTrackThatIsSounding() {
        Score score = new Score("", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo")));
        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(1, 0, 0));
        Painted painted = paint(score, new Cursor(0, 0, 0, 1), playhead);

        Rectangle beat = painted.layout().beatBounds(1, 0, 0);
        int nearSystemTop = painted.layout().trackTop(0, 0) + 5;

        assertTrue(
                nearSystemTop < painted.layout().staffTop(1, 0),
                "el punto de control tiene que estar arriba de la pista que suena, no adentro");
        assertTrue(
                painted.hasInkNear(beat.x, nearSystemTop, 0),
                "la linea tiene que cruzar tambien la pista de arriba, no solo la que suena");
    }

    @Test
    void writesTheScoresTempoAboveTheFirstMeasure() {
        Painted painted = paint(Score.blank(), new Cursor(0, 0, 0, 1), Playhead.silent());

        Rectangle beat = painted.layout().beatBounds(0, 0, 0);
        int staffTop = painted.layout().staffTop(0, 0);
        Rectangle above = new Rectangle(
                beat.x, staffTop - ScoreLayout.STAFF_HEADROOM, beat.width, ScoreLayout.STAFF_HEADROOM);

        assertTrue(painted.hasColorIn(above, ScoreColors.TEMPO),
                "el tempo global de la partitura tiene que verse arriba del primer compas");
    }

    @Test
    void theInitialTempoDoesNotDuplicateAnExplicitChangeOnTheFirstBeat() {
        Measure withExplicitChange = measureOf(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)),
                Beat.of(Duration.quarter(), new Note(1, 5)));
        ParameterChange explicitTempo = ParameterChange.nothing().changing(SoundParameter.TEMPO, 90);
        withExplicitChange = withExplicitChange.withBeat(0,
                withExplicitChange.beat(0).withEffects(BeatEffects.none().withParameterChange(explicitTempo)));
        Score score = new Score("", 120, List.of(
                new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(withExplicitChange))));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        LienzoDePrueba lienzo = new LienzoDePrueba();

        ScorePainter.paint(lienzo, layout, score, new Cursor(0, 0, 0, 1), Playhead.silent());

        long tempoGlyphs = lienzo.textosDibujados().stream()
                .filter(texto -> MusicFont.metNoteQuarterUp().equals(texto.texto()))
                .count();
        assertEquals(1, tempoGlyphs, "el compas 1 solo tiene que mostrar un tempo, el del cambio explicito");
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

    /**
     * El manual describe seis tipos de slide (linea 1250 y siguientes). Cada uno tiene que
     * dibujarse distinto de no tener slide, y distinto de los otros cinco -no alcanza con que el
     * enum tenga seis valores si despues el pintor los confunde.
     */
    @Test
    void everySlideTypeDrawsSomethingDifferentOnTheTablature() {
        Painted sinSlide = paint(scoreWith(measureWithSlide(null)), new Cursor(0, 0, 0, 1), Playhead.silent());

        List<Painted> conCadaTipo = new ArrayList<>();
        for (com.gstncaruso.tabpro.core.model.effects.SlideType tipo :
                com.gstncaruso.tabpro.core.model.effects.SlideType.values()) {
            Painted painted = paint(scoreWith(measureWithSlide(tipo)), new Cursor(0, 0, 0, 1), Playhead.silent());
            assertFalse(painted.looksLike(sinSlide), "el " + tipo + " no dibuja nada distinto de no tener slide");
            conCadaTipo.add(painted);
        }

        for (int i = 0; i < conCadaTipo.size(); i++) {
            for (int j = i + 1; j < conCadaTipo.size(); j++) {
                assertFalse(conCadaTipo.get(i).looksLike(conCadaTipo.get(j)),
                        "el pintor dibuja lo mismo para "
                                + com.gstncaruso.tabpro.core.model.effects.SlideType.values()[i] + " y "
                                + com.gstncaruso.tabpro.core.model.effects.SlideType.values()[j]);
            }
        }
    }

    private static Measure measureWithSlide(com.gstncaruso.tabpro.core.model.effects.SlideType tipo) {
        Note primera = tipo == null ? new Note(1, 3) : new Note(1, 3).withSlide(tipo);
        return measureOf(
                Beat.of(Duration.quarter(), primera),
                Beat.of(Duration.quarter(), new Note(1, 7)),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()));
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

    /**
     * El manual (p14: «Bridge», «Outro») dibuja un cuadradito solido con el color del marcador
     * junto a su nombre. tabpro lo ponia arriba del texto, en la misma franja donde escribe el
     * nombre de la pista -algo que GP5 no hace ahi-, y el cuadrado terminaba pisandolo. Ahora
     * comparte renglon con el texto del marcador, a su izquierda.
     */
    @Test
    void aSectionMarkerDrawsASquareInItsOwnColorBesideItsName() {
        Color markerColor = new Color(0x00, 0xAA, 0x00);
        Painted painted = paintWithMarkerColor(markerColor);

        Rectangle square = markerSquareBounds(painted);

        assertEquals(markerColor.getRGB(),
                painted.image().getRGB(square.x + square.width / 2, square.y + square.height / 2),
                "el marcador tiene que dibujar un cuadrado solido con su propio color junto al nombre");
    }

    @Test
    void theSectionMarkerSquareSharesBaselineWithItsText() {
        Color markerColor = new Color(0x00, 0xAA, 0x00);
        Painted painted = paintWithMarkerColor(markerColor);

        Rectangle square = markerSquareBounds(painted);
        int textBaseline = painted.layout().staffTop(0, 0) - BarStructurePainter.MARKER_TEXT_CLEARANCE_ABOVE_STAFF;

        assertTrue(painted.hasColorIn(new Rectangle(square.x, textBaseline - 1, square.width, 1), markerColor),
                "el cuadrado tiene que apoyar su base en la linea de base del texto del marcador");
    }

    @Test
    void theSectionMarkerSquareDoesNotOverlapTheTrackName() {
        Color markerColor = new Color(0xCC, 0x00, 0x00);
        Painted painted = paintWithMarkerColor(markerColor);

        Rectangle trackName = trackNameBounds(painted, "Guitarra");

        assertFalse(painted.hasColorIn(trackName, markerColor),
                "el cuadrado del marcador no puede pisar el nombre de la pista");
    }

    @Test
    void aSectionMarkerSquareGetsAnInkOutlineWhenItsColorDoesNotContrastWithTheBackground() {
        Color lowContrastColor = new Color(0x35, 0x37, 0x3B);
        Painted painted = paintWithMarkerColor(lowContrastColor);

        Rectangle square = markerSquareBounds(painted);
        Color edge = new Color(painted.image().getRGB(square.x, square.y));
        Color interior = new Color(painted.image().getRGB(
                square.x + square.width / 2, square.y + square.height / 2));

        assertNotEquals(edge.getRGB(), interior.getRGB(),
                "un marcador que no contrasta con el fondo necesita un borde de tinta alrededor del cuadrado");
    }

    @Test
    void aSectionMarkerSquareHasNoExtraOutlineWhenItsColorAlreadyContrasts() {
        Color highContrastColor = new Color(0x00, 0xAA, 0x00);
        Painted painted = paintWithMarkerColor(highContrastColor);

        Rectangle square = markerSquareBounds(painted);
        Color edge = new Color(painted.image().getRGB(square.x, square.y));
        Color interior = new Color(painted.image().getRGB(
                square.x + square.width / 2, square.y + square.height / 2));

        assertEquals(edge.getRGB(), interior.getRGB(),
                "un marcador que ya contrasta con el fondo no necesita un borde extra");
    }

    /**
     * El manual (p14: «Outro») mide el cuadradito de color en 6x9 px a 96 dpi; a la escala
     * interna de tabpro, x1,333, eso da 8x12.
     */
    @Test
    void theSectionMarkerSquareMatchesTheSizeMeasuredInTheManual() {
        Color markerColor = new Color(0x00, 0xAA, 0x00);
        Painted painted = paintWithMarkerColor(markerColor);

        Rectangle square = solidMarkerSquareBounds(painted, markerColor);

        assertEquals(8, square.width, "el cuadrado tiene que medir 8 px de ancho, como en el manual");
        assertEquals(12, square.height, "el cuadrado tiene que medir 12 px de alto, como en el manual");
    }

    /**
     * El nombre del marcador comparte color con el cuadrado, asi que no alcanza con acotar el
     * area de busqueda: hay que parar en la primera columna en blanco despues del cuadrado, antes
     * de llegar al texto.
     */
    private static Rectangle solidMarkerSquareBounds(Painted painted, Color markerColor) {
        int x0 = painted.layout().measureX(0);
        int staffTop = painted.layout().staffTop(0, 0);
        int yTop = staffTop - 40;
        int rgb = markerColor.getRGB();

        int rightEdge = x0 - 1;
        boolean sawColor = false;
        for (int x = x0; x < x0 + 60; x++) {
            boolean columnHasColor = false;
            for (int y = yTop; y < staffTop; y++) {
                if (painted.image().getRGB(x, y) == rgb) {
                    columnHasColor = true;
                    break;
                }
            }
            if (columnHasColor) {
                sawColor = true;
                rightEdge = x;
            } else if (sawColor) {
                break;
            }
        }

        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int x = x0; x <= rightEdge; x++) {
            for (int y = yTop; y < staffTop; y++) {
                if (painted.image().getRGB(x, y) == rgb) {
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return new Rectangle(x0, minY, rightEdge - x0 + 1, maxY - minY + 1);
    }

    private static Painted paintWithMarkerColor(Color color) {
        Measure marked = measureOf(Beat.of(Duration.quarter(), new Note(1, 0)))
                .mappingAttributes(attrs -> attrs.withMarker(new com.gstncaruso.tabpro.core.model.bars.Marker(
                        "Intro", new com.gstncaruso.tabpro.core.model.ScoreColor(
                                color.getRed(), color.getGreen(), color.getBlue()))));
        return paint(scoreWith(marked), new Cursor(0, 0, 0, 1), Playhead.silent());
    }

    private static Rectangle markerSquareBounds(Painted painted) {
        int x = painted.layout().measureX(0);
        int staffTop = painted.layout().staffTop(0, 0);
        int textBaseline = staffTop - BarStructurePainter.MARKER_TEXT_CLEARANCE_ABOVE_STAFF;
        return new Rectangle(x, textBaseline - BarStructurePainter.MARKER_SQUARE_HEIGHT,
                BarStructurePainter.MARKER_SQUARE_WIDTH, BarStructurePainter.MARKER_SQUARE_HEIGHT);
    }

    private static Rectangle trackNameBounds(Painted painted, String trackName) {
        int x = painted.layout().measureX(0);
        FontMetrics metrics = painted.image().createGraphics().getFontMetrics(ScoreFonts.TRACK_LABEL_FONT);
        int baseline = painted.layout().trackTop(0, 0) + ScoreLayout.TRACK_LABEL_HEIGHT - 4;
        return new Rectangle(x, baseline - metrics.getAscent(),
                metrics.stringWidth(trackName), metrics.getAscent() + metrics.getDescent());
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
    void chordDiagramPaintsBelowTheStaffWhenTheTrackAsksFor() {
        Measure measure = measureOf(
                Beat.of(Duration.quarter(), new Note(1, 0)).withEffects(
                        com.gstncaruso.tabpro.core.model.effects.BeatEffects.none().withChord(
                                com.gstncaruso.tabpro.core.model.chords.ChordDiagram.named(
                                        "Do", List.of(-1, 3, 2, 0, 1, 0)))),
                Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure))
                .mappingSettings(settings -> settings.withDisplay(
                        settings.display().withDiagramsBelowStandardNotation(true)));
        Score score = new Score("", 120, List.of(track));

        Painted painted = paint(score, new Cursor(0, 0, 0, 1), Playhead.silent());

        Rectangle beat = painted.layout().beatBounds(0, 0, 0);
        int x = beat.x + beat.width / 2;
        int aboveTheStaff = painted.layout().staffTop(0, 0) - 12;
        int belowTheStaff = painted.layout().staffBottom(0, 0) + 20;
        assertFalse(painted.hasInkNear(x, aboveTheStaff, 3), "no deberia dibujarse arriba del pentagrama");
        assertTrue(painted.hasInkNear(x, belowTheStaff, 8), "deberia dibujarse debajo del pentagrama");
    }

    @Test
    void anIncompleteMeasureThatIsNotBeingEditedIsOutlinedInItsWarningColour() {
        Measure incomplete = measureOf(Beat.of(Duration.quarter(), new Note(1, 0)));
        Measure complete = measureOf(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)));
        Score score = scoreWith(incomplete, complete);

        Painted painted = paint(score, new Cursor(0, 1, 0, 1), Playhead.silent());

        Rectangle bounds = painted.layout().measureBounds(0, 0);
        int edge = painted.image().getRGB(bounds.x + bounds.width / 2, bounds.y);
        int centre = painted.image().getRGB(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        assertNotEquals(edge, centre,
                "el borde de aviso tiene que verse distinto del tinte que cubre el resto del compas");
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
    void theSelectionIsAlsoMarkedWithASolidBorder() {
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

        Rectangle bounds = layout.measureBounds(0, 0);
        int y = bounds.y + bounds.height / 2;
        int edge = image.getRGB(bounds.x, y);
        int centre = image.getRGB(bounds.x + bounds.width / 2, y);
        assertNotEquals(edge, centre,
                "la seleccion necesita un borde solido, distinto del relleno translucido");
    }

    @Test
    void theSelectionFillIsGuitarPro5sYellowNotTheOldTranslucentBlue() {
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

        Rectangle bounds = layout.measureBounds(0, 0);
        int interiorX = bounds.x + bounds.width / 2;
        int interiorY = bounds.y + bounds.height / 2;
        Color expectedFill = com.gstncaruso.tabpro.ui.theme.PaletteCheck.compositeOver(
                new Color(0xFF, 0xFF, 0x00, 0x50), ScoreColors.BACKGROUND);

        assertEquals(expectedFill.getRGB(), image.getRGB(interiorX, interiorY),
                "el manual mide la seleccion de Guitar Pro 5 en amarillo #FFFF00, no en azul");
    }

    @Test
    void theSelectionBorderIsSolidYellowLikeGuitarPro5NotTheAccentBlue() {
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

        Rectangle bounds = layout.measureBounds(0, 0);
        int y = bounds.y + bounds.height / 2;
        Color edge = new Color(image.getRGB(bounds.x, y));

        assertTrue(edge.getRed() > 180 && edge.getGreen() > 180 && edge.getBlue() < 60,
                "el borde de la seleccion tiene que ser amarillo solido (rojo y verde altos, azul bajo), "
                        + "no el azul de ACCENT: " + edge);
    }

    @Test
    void aContiguousMultiBeatSelectionIsOutlinedAsOneAreaWithoutInternalLines() {
        Measure full = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3))));
        Score score = scoreWith(full);
        com.gstncaruso.tabpro.core.editing.Selection selection =
                new com.gstncaruso.tabpro.core.editing.Selection(0, 0, 0, 0, 1, false);
        ScoreLayout layout = ScoreLayout.of(score, WIDTH);
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        ScorePainter.paint(
                g, layout, score, new Cursor(0, 0, 0, 1), Playhead.silent(), java.util.Optional.of(selection));
        g.dispose();

        Rectangle first = layout.beatBounds(0, 0, 0);
        Rectangle second = layout.beatBounds(0, 0, 1);
        int y = first.y + first.height / 2;
        int interior = image.getRGB(first.x + first.width / 2, y);
        int internalBoundary = image.getRGB(second.x, y);
        int leftEdge = image.getRGB(first.x, y);
        int rightEdge = image.getRGB(second.x + second.width - 1, y);

        assertEquals(interior, internalBoundary,
                "no tiene que haber una linea de borde entre los dos beats seleccionados y contiguos");
        assertNotEquals(interior, leftEdge, "el borde izquierdo del area completa tiene que verse");
        assertNotEquals(interior, rightEdge, "el borde derecho del area completa tiene que verse");
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
    void doesNotDrawTheTuningLegendByDefault() {
        Track guitar = Track.standardGuitar("Guitarra");
        Track withLegendOff = guitar.mappingSettings(
                settings -> settings.withDisplay(settings.display().withTuningLegend(false)));

        Painted byDefault = paint(new Score("", 120, List.of(guitar)), new Cursor(0, 0, 0, 1), Playhead.silent());
        Painted off = paint(new Score("", 120, List.of(withLegendOff)), new Cursor(0, 0, 0, 1), Playhead.silent());

        assertTrue(byDefault.looksLike(off), "una pista nueva no tiene que mostrar los nombres de cuerda");
    }

    @Test
    void drawsTheTuningLegendWhenTheTrackAsksForIt() {
        Track guitar = Track.standardGuitar("Guitarra");
        Track withLegendOn = guitar.mappingSettings(
                settings -> settings.withDisplay(settings.display().withTuningLegend(true)));

        Painted withoutLegend = paint(new Score("", 120, List.of(guitar)), new Cursor(0, 0, 0, 1), Playhead.silent());
        Painted withLegend = paint(new Score("", 120, List.of(withLegendOn)), new Cursor(0, 0, 0, 1), Playhead.silent());

        assertFalse(withoutLegend.looksLike(withLegend),
                "tildar la casilla de afinacion tiene que dibujar los nombres de cuerda");
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


    /**
     * Todas las pistas suenan a la vez, asi que la reproduccion esta en UN solo lugar: una sola
     * linea. Como cada pista parte el compas distinto -la guitarra en negras, el bajo en una
     * redonda- el arranque del beat que suena cae en una x distinta por pista, y dibujar una
     * linea por pista llenaria el sistema de lineas paralelas. La que vale es la del beat que
     * arranco mas tarde: es la que esta mas cerca del instante que se esta oyendo.
     */
    @Test
    void thereIsOnlyOnePlayingLineNoMatterHowManyTracksAreSounding() {
        Score score = twoTracksSplittingTheBarDifferently();
        BeatPosition inTheGuitar = new BeatPosition(0, 0, 2);
        Playhead playhead = Playhead.silent()
                .advancedTo(inTheGuitar)
                .advancedTo(new BeatPosition(1, 0, 0));
        Painted painted = paint(score, new Cursor(0, 0, 0, 1), playhead);

        int acrossTheBass = painted.layout().tabTop(1, 0) + 3;
        List<Integer> lines = painted.playingColumnsAt(acrossTheBass);

        assertEquals(1, lines.size(), "una sola linea de reproduccion, no una por pista");
        assertEquals(
                painted.layout().beatBounds(0, 0, 2).x, lines.get(0),
                "la linea va donde arranco el beat que suena mas tarde");
    }

    private static Score twoTracksSplittingTheBarDifferently() {
        Track guitar = Track.standardGuitar("Guitarra");
        Track bass = Track.standardBass("Bajo");
        Measure inQuarters = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(6, 0)),
                Beat.of(Duration.quarter(), new Note(6, 2)),
                Beat.of(Duration.quarter(), new Note(6, 3)),
                Beat.of(Duration.quarter(), new Note(6, 5))));
        Measure inOneWhole = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(new Duration(NoteValue.WHOLE, false), new Note(4, 0))));
        return new Score("", 120, List.of(
                new Track("Guitarra", guitar.tuning(), guitar.channel(), List.of(inQuarters)),
                new Track("Bajo", bass.tuning(), bass.channel(), List.of(inOneWhole))));
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

        /** Las columnas pintadas con el color de la reproduccion a esa altura. */
        List<Integer> playingColumnsAt(int y) {
            List<Integer> columns = new ArrayList<>();
            for (int x = 0; x < image.getWidth(); x++) {
                if (isInside(x, y) && image.getRGB(x, y) == ScoreColors.PLAYING.getRGB()) {
                    columns.add(x);
                }
            }
            return columns;
        }

        boolean hasColorIn(Rectangle area, Color color) {
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    if (isInside(x, y) && image.getRGB(x, y) == color.getRGB()) {
                        return true;
                    }
                }
            }
            return false;
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
