package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ScoreLayoutTest {

    private static final int WIDE = 4000;

    @Test
    void aShortScoreFitsInOneSystem() {
        ScoreLayout layout = ScoreLayout.of(Score.blank(), WIDE);

        assertEquals(1, layout.systemCount());
        assertEquals(0, layout.systemOf(0));
    }

    @Test
    void measuresThatDoNotFitWrapToTheNextSystem() {
        Score score = scoreWithMeasures(12);

        ScoreLayout layout = ScoreLayout.of(score, 300);

        assertTrue(layout.systemCount() > 1);
        assertEquals(0, layout.systemOf(0));
        assertEquals(layout.systemCount() - 1, layout.systemOf(11));
    }

    @Test
    void everySystemStartsAtTheLeftMargin() {
        Score score = scoreWithMeasures(12);

        ScoreLayout layout = ScoreLayout.of(score, 300);

        int firstOfSecondSystem = firstMeasureOfSystem(layout, 1, 12);
        assertEquals(ScoreLayout.LEFT_MARGIN, layout.measureX(0));
        assertEquals(ScoreLayout.LEFT_MARGIN, layout.measureX(firstOfSecondSystem));
    }

    @Test
    void aColumnIsAsWideAsTheBusiestTrackNeeds() {
        Score sparse = new Score("", 120, List.of(trackWith(quarters(1))));
        Score busy = new Score("", 120, List.of(trackWith(quarters(1)), trackWith(eighths(8))));

        int sparseWidth = ScoreLayout.of(sparse, WIDE).measureWidth(0);
        int busyWidth = ScoreLayout.of(busy, WIDE).measureWidth(0);

        assertTrue(busyWidth > sparseWidth, "el compas con ocho corcheas tiene que pedir mas ancho");
    }

    @Test
    void everyTrackDrawsTheSameMeasureAtTheSameHorizontalPlace() {
        Score score = new Score("", 120, List.of(trackWith(quarters(4)), trackWith(eighths(8))));

        ScoreLayout layout = ScoreLayout.of(score, WIDE);

        assertEquals(layout.measureBounds(0, 0).x, layout.measureBounds(1, 0).x);
        assertEquals(layout.measureBounds(0, 0).width, layout.measureBounds(1, 0).width);
    }

    @Test
    void tracksAreStackedOneBelowTheOther() {
        Score score = new Score("", 120, List.of(trackWith(quarters(4)), trackWith(quarters(4))));

        ScoreLayout layout = ScoreLayout.of(score, WIDE);

        assertTrue(layout.trackTop(1, 0) > layout.trackTop(0, 0));
        assertTrue(layout.staffTop(0, 0) < layout.tabTop(0, 0));
        assertTrue(layout.tabTop(0, 0) < layout.staffTop(1, 0));
    }

    @Test
    void theStaffSitsAboveTheTablatureOfItsOwnTrack() {
        ScoreLayout layout = ScoreLayout.of(Score.blank(), WIDE);

        assertEquals(ScoreLayout.STAFF_HEIGHT, layout.staffBottom(0, 0) - layout.staffTop(0, 0));
        assertTrue(layout.tabTop(0, 0) > layout.staffBottom(0, 0));
    }

    @Test
    void stringsAreEvenlySpacedDownFromTheTopOfTheTablature() {
        ScoreLayout layout = ScoreLayout.of(Score.blank(), WIDE);

        assertEquals(layout.tabTop(0, 0), layout.stringY(0, 0, 1));
        assertEquals(layout.tabTop(0, 0) + ScoreLayout.STRING_SPACING, layout.stringY(0, 0, 2));
        assertEquals(5 * ScoreLayout.STRING_SPACING, layout.stringY(0, 0, 6) - layout.stringY(0, 0, 1));
    }

    @Test
    void theBeatsOfAMeasureFillItsColumn() {
        Score score = new Score("", 120, List.of(trackWith(quarters(4)), trackWith(eighths(8))));

        ScoreLayout layout = ScoreLayout.of(score, WIDE);

        Rectangle first = layout.beatBounds(0, 0, 0);
        Rectangle last = layout.beatBounds(0, 0, 3);
        int notesStart = layout.measureX(0) + layout.headWidth(0);
        int columnEnd = layout.measureX(0) + layout.measureWidth(0);
        assertEquals(notesStart + ScoreLayout.MEASURE_LEFT_PADDING, first.x);
        assertEquals(columnEnd - ScoreLayout.MEASURE_RIGHT_PADDING, last.x + last.width);
    }

    @Test
    void theFirstMeasureOfEachSystemReservesRoomForTheClef() {
        Score score = scoreWithMeasures(12);

        ScoreLayout layout = ScoreLayout.of(score, 800);

        int firstOfSecondSystem = firstMeasureOfSystem(layout, 1, 12);
        assertTrue(firstOfSecondSystem > 1, "el sistema tiene que entrar mas de un compas");
        assertTrue(layout.startsASystem(0));
        assertTrue(layout.startsASystem(firstOfSecondSystem));
        assertEquals(ScoreLayout.SYSTEM_HEAD_WIDTH, layout.headWidth(0));
        assertEquals(0, layout.headWidth(firstOfSecondSystem - 1));
        assertTrue(layout.measureWidth(0) > layout.measureWidth(firstOfSecondSystem - 1));
    }

    @Test
    void withoutAnyLineBreaksTheLayoutIsExactlyTheAutomaticOne() {
        Score score = scoreWithMeasures(12);

        ScoreLayout layout = ScoreLayout.of(score, 800);

        assertEquals(0, layout.systemOf(0));
        assertEquals(0, layout.systemOf(1));
        assertEquals(0, layout.systemOf(2));
        assertEquals(1, layout.systemOf(3));
        assertEquals(1, layout.systemOf(4));
        assertEquals(1, layout.systemOf(5));
        assertEquals(2, layout.systemOf(6));
        assertEquals(3, layout.systemOf(9));
        assertEquals(4, layout.systemCount());
    }

    @Test
    void aForcedBreakStartsANewSystemEvenWithRoomToSpare() {
        Score score = withLineBreakAt(scoreWithMeasures(5), 2, LineBreak.FORCED);

        ScoreLayout layout = ScoreLayout.of(score, WIDE);

        assertEquals(0, layout.systemOf(0));
        assertEquals(0, layout.systemOf(1));
        assertEquals(1, layout.systemOf(2));
        assertTrue(layout.startsASystem(2), "el compas forzado tiene que arrancar el sistema");
        assertEquals(1, layout.systemOf(3), "sobra ancho, asi que el resto sigue en el mismo sistema");
        assertEquals(2, layout.systemCount());
    }

    @Test
    void twoForcedBreaksMakeThreeSystems() {
        Score withOneBreak = withLineBreakAt(scoreWithMeasures(6), 2, LineBreak.FORCED);
        Score score = withLineBreakAt(withOneBreak, 4, LineBreak.FORCED);

        ScoreLayout layout = ScoreLayout.of(score, WIDE);

        assertEquals(0, layout.systemOf(1));
        assertEquals(1, layout.systemOf(2));
        assertEquals(1, layout.systemOf(3));
        assertEquals(2, layout.systemOf(4));
        assertEquals(2, layout.systemOf(5));
        assertEquals(3, layout.systemCount());
    }

    @Test
    void aPreventedBreakStaysInTheCurrentSystemEvenIfTheWidthAsksForABreak() {
        Score score = withLineBreakAt(scoreWithMeasures(12), 3, LineBreak.PREVENTED);

        ScoreLayout layout = ScoreLayout.of(score, 800);

        assertEquals(0, layout.systemOf(3), "el compas impedido se queda en el sistema anterior, que se estira");
        assertFalse(layout.startsASystem(3));
        assertEquals(1, layout.systemOf(4), "el corte se corre al compas siguiente que si puede arrancar sistema");
        assertTrue(layout.startsASystem(4));
    }

    /**
     * El manual dice que el salto de linea vale solo para la pista activa, o para la vista
     * multipista: fuera de la vista multipista, el corte de una pista no puede aparecer en el
     * layout de otra pista.
     */
    @Test
    void outsideTheMultitrackViewOnlyTheActiveTracksOwnLineBreakApplies() {
        Score score = withLineBreakOnTrack(twoTrackScoreWithMeasures(5), 1, 2, LineBreak.FORCED);
        VisibleTracks secondTrackActive = VisibleTracks.all().withMultitrack(false).withActiveTrack(1);
        VisibleTracks firstTrackActive = VisibleTracks.all().withMultitrack(false).withActiveTrack(0);

        ScoreLayout onSecondTrack = ScoreLayout.of(score, WIDE, secondTrackActive);
        ScoreLayout onFirstTrack = ScoreLayout.of(score, WIDE, firstTrackActive);

        assertTrue(onSecondTrack.startsASystem(2),
                "el corte forzado en la pista activa tiene que arrancar un sistema nuevo");
        assertFalse(onFirstTrack.startsASystem(2),
                "el corte de la otra pista no tiene que verse mientras esa pista no es la activa");
    }

    @Test
    void theMultitrackViewUsesItsOwnSharedLineBreaksRegardlessOfTheActiveTrack() {
        Score score = withLineBreakOnTrack(twoTrackScoreWithMeasures(5), 0, 2, LineBreak.FORCED);
        VisibleTracks multitrackWithSecondActive = VisibleTracks.all().withActiveTrack(1);

        ScoreLayout layout = ScoreLayout.of(score, WIDE, multitrackWithSecondActive);

        assertTrue(layout.startsASystem(2),
                "en la vista multipista el corte compartido vale sin importar cual pista este activa");
    }

    @Test
    void resettingTheLineBreakGoesBackToTheAutomaticLayout() {
        Score automatic = scoreWithMeasures(12);
        Score forced = withLineBreakAt(automatic, 1, LineBreak.FORCED);
        Score reset = withLineBreakAt(forced, 1, LineBreak.AUTOMATIC);

        ScoreLayout forcedLayout = ScoreLayout.of(forced, 800);
        ScoreLayout resetLayout = ScoreLayout.of(reset, 800);
        ScoreLayout automaticLayout = ScoreLayout.of(automatic, 800);

        assertEquals(1, forcedLayout.systemOf(1), "forzado, el compas 1 arranca su propio sistema");
        assertEquals(automaticLayout.systemOf(1), resetLayout.systemOf(1),
                "reiniciado, vuelve a repartirse igual que si nunca se hubiera tocado");
    }

    @Test
    void aLongerFigureGetsAWiderSlotThanAShorterOne() {
        Measure mixed = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(new Duration(NoteValue.HALF, false), new Note(1, 0)),
                Beat.of(new Duration(NoteValue.EIGHTH, false), new Note(1, 0))));
        Score score = new Score("", 120, List.of(trackWith(mixed)));

        ScoreLayout layout = ScoreLayout.of(score, WIDE);

        assertTrue(layout.beatBounds(0, 0, 0).width > layout.beatBounds(0, 0, 1).width);
    }

    @Test
    void findsTheCellUnderThePointer() {
        Score score = new Score("", 120, List.of(trackWith(quarters(4)), trackWith(quarters(4))));
        ScoreLayout layout = ScoreLayout.of(score, WIDE);
        Rectangle target = layout.beatBounds(1, 0, 2);

        Optional<ScoreLayout.Hit> hit = layout.hitTest(
                target.x + target.width / 2, layout.stringY(1, 0, 3));

        assertEquals(Optional.of(new ScoreLayout.Hit(1, 0, 2, 3)), hit);
    }

    @Test
    void aClickOnTheStaffFindsTheSameBeatAsAClickOnTheTablature() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(6, 0)));
        ScoreLayout layout = ScoreLayout.of(score, WIDE);
        Rectangle target = layout.beatBounds(0, 0, 0);
        int staffY = layout.staffTop(0, 0) + 2;

        Optional<ScoreLayout.Hit> hit = layout.hitTest(target.x + target.width / 2, staffY);

        assertTrue(hit.isPresent(), "un clic en el pentagrama tiene que encontrar el compas y el beat");
        assertEquals(0, hit.get().measure());
        assertEquals(0, hit.get().beat());
    }

    @Test
    void aClickOnTheStaffFindsTheStringOfTheNearestNote() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(6, 0)));
        ScoreLayout layout = ScoreLayout.of(score, WIDE);
        Rectangle target = layout.beatBounds(0, 0, 0);
        int staffY = layout.staffTop(0, 0);

        Optional<ScoreLayout.Hit> hit = layout.hitTest(target.x + target.width / 2, staffY);

        assertEquals(6, hit.get().string());
    }

    private static Score scoreWith(Beat beat) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        return new Score("", 120, List.of(trackWith(measure)));
    }

    @Test
    void findsNothingBeyondTheScore() {
        ScoreLayout layout = ScoreLayout.of(Score.blank(), WIDE);

        assertEquals(Optional.empty(), layout.hitTest(-50, -50));
        assertEquals(Optional.empty(), layout.hitTest(10, 100_000));
    }

    @Test
    void aTrackWithFewerMeasuresSimplyHasNoneThere() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track shortTrack = trackWith(measure);
        Track longTrack = new Track("Larga", shortTrack.tuning(), shortTrack.channel(), List.of(measure, measure));
        Score score = new Score("", 120, List.of(shortTrack, longTrack));

        ScoreLayout layout = ScoreLayout.of(score, WIDE);

        assertTrue(layout.hasMeasure(1, 1));
        assertTrue(!layout.hasMeasure(0, 1));
        assertEquals(2, layout.measureCount());
    }

    @Test
    void growsTallerWithEachSystem() {
        Score score = scoreWithMeasures(12);

        int oneSystem = ScoreLayout.of(score, WIDE).totalHeight();
        int manySystems = ScoreLayout.of(score, 300).totalHeight();

        assertTrue(manySystems > oneSystem);
        assertNotEquals(0, oneSystem);
    }

    @Test
    void aMidSystemTimeSignatureChangeReservesRoom() {
        Measure fourFour = quarters(4);
        Measure threeFour = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 0)))).withTimeSignature(new TimeSignature(3, 4));
        Track guitar = Track.standardGuitar("Guitarra");
        Score score = new Score("", 120, List.of(new Track(
                "Guitarra", guitar.tuning(), guitar.channel(), List.of(fourFour, threeFour, threeFour))));

        ScoreLayout layout = ScoreLayout.of(score, WIDE);

        assertFalse(layout.startsASystem(1), "sigue siendo el mismo sistema");
        assertTrue(layout.hasSignatureChange(1));
        assertEquals(ScoreLayout.SIGNATURE_CHANGE_WIDTH, layout.headWidth(1));
        assertFalse(layout.hasSignatureChange(2), "el compas 2 sigue en el mismo compas que el 1, no cambia nada");
        assertFalse(layout.hasSignatureChange(0), "el primer compas de la partitura no cambia nada");
    }

    @Test
    void aTrackThatIsNotShownTakesNoRoom() {
        Score score = new Score("", 120, List.of(trackWith(quarters(4)), trackWith(quarters(4))));
        VisibleTracks withoutTheFirst = VisibleTracks.all().withActiveTrack(1).withTrackShown(0, false);

        ScoreLayout both = ScoreLayout.of(score, WIDE);
        ScoreLayout onlyTheSecond = ScoreLayout.of(score, WIDE, withoutTheFirst);

        assertTrue(onlyTheSecond.totalHeight() < both.totalHeight());
        assertEquals(both.trackTop(0, 0), onlyTheSecond.trackTop(1, 0),
                "la pista que queda sube al lugar de la que se apago");
    }

    @Test
    void aTrackThatIsNotShownIsNotUnderThePointerEither() {
        Score score = new Score("", 120, List.of(trackWith(quarters(4)), trackWith(quarters(4))));
        VisibleTracks withoutTheFirst = VisibleTracks.all().withActiveTrack(1).withTrackShown(0, false);
        ScoreLayout layout = ScoreLayout.of(score, WIDE, withoutTheFirst);
        Rectangle target = layout.beatBounds(1, 0, 2);

        Optional<ScoreLayout.Hit> hit = layout.hitTest(
                target.x + target.width / 2, layout.stringY(1, 0, 3));

        assertEquals(1, hit.orElseThrow().track());
    }

    @Test
    void withoutTheMultitrackViewOnlyTheActiveTrackTakesRoom() {
        Score score = new Score("", 120, List.of(trackWith(quarters(4)), trackWith(quarters(4))));
        VisibleTracks single = VisibleTracks.all().withMultitrack(false).withActiveTrack(1);

        ScoreLayout layout = ScoreLayout.of(score, WIDE, single);

        assertEquals(ScoreLayout.TOP_MARGIN, layout.trackTop(1, 0));
        assertEquals(0, layout.trackHeight(0));
    }

    @Test
    void theFirstTrackThatShowsIsTheOneThatCarriesTheBarStructure() {
        Score score = new Score("", 120, List.of(trackWith(quarters(4)), trackWith(quarters(4))));

        assertEquals(0, ScoreLayout.of(score, WIDE).firstShownTrack());
        assertEquals(1, ScoreLayout.of(score, WIDE,
                VisibleTracks.all().withActiveTrack(1).withTrackShown(0, false)).firstShownTrack());
    }

    @Test
    void hidingTheStaffLiftsTheTablatureIntoItsPlace() {
        Score score = Score.blank();

        ScoreLayout both = ScoreLayout.of(score, WIDE);
        ScoreLayout onlyTablature = ScoreLayout.of(score, WIDE, VisibleTracks.all(),
                VisibleNotations.both().withStandardNotation(false));

        assertTrue(onlyTablature.totalHeight() < both.totalHeight());
        assertEquals(both.staffTop(0, 0), onlyTablature.tabTop(0, 0),
                "la tablatura sube al lugar donde arrancaba el pentagrama");
    }

    @Test
    void hidingTheTablatureLeavesTheStaffWhereItWas() {
        Score score = Score.blank();

        ScoreLayout both = ScoreLayout.of(score, WIDE);
        ScoreLayout onlyStaff = ScoreLayout.of(score, WIDE, VisibleTracks.all(),
                VisibleNotations.both().withTablature(false));

        assertEquals(both.staffTop(0, 0), onlyStaff.staffTop(0, 0));
        assertEquals(both.staffBottom(0, 0), onlyStaff.staffBottom(0, 0));
        assertTrue(onlyStaff.totalHeight() < both.totalHeight());
        assertEquals(onlyStaff.tabTop(0, 0), onlyStaff.tabBottom(0, 0),
                "sin tablatura, la franja de la tablatura no mide nada");
    }

    @Test
    void theLayoutAnswersWhichNotationsEachTrackDraws() {
        ScoreLayout onlyTablature = ScoreLayout.of(Score.blank(), WIDE, VisibleTracks.all(),
                VisibleNotations.both().withStandardNotation(false));

        assertFalse(onlyTablature.showsStandardNotation(0));
        assertTrue(onlyTablature.showsTablature(0));
    }

    /** Ver > Notas con dinamica [F11]: por defecto no se muestran, para no recargar la hoja. */
    @Test
    void byDefaultTheLayoutDoesNotShowDynamicNotes() {
        assertFalse(ScoreLayout.of(Score.blank(), WIDE).showsDynamicNotes());
    }

    @Test
    void theLayoutCanBeAskedToShowDynamicNotes() {
        ScoreLayout layout = ScoreLayout.of(
                Score.blank(), WIDE, VisibleTracks.all(), VisibleNotations.both(), true);

        assertTrue(layout.showsDynamicNotes());
    }

    private int firstMeasureOfSystem(ScoreLayout layout, int system, int measureCount) {
        for (int measure = 0; measure < measureCount; measure++) {
            if (layout.systemOf(measure) == system) {
                return measure;
            }
        }
        throw new AssertionError("no hay un compas en el sistema " + system);
    }

    private static Score withLineBreakAt(Score score, int measureIndex, LineBreak lineBreak) {
        return withLineBreakOnTrack(score, 0, measureIndex, lineBreak);
    }

    private static Score withLineBreakOnTrack(Score score, int trackIndex, int measureIndex, LineBreak lineBreak) {
        return score.mappingTrack(trackIndex, track -> track.mappingMeasure(measureIndex,
                measure -> measure.mappingAttributes(attributes -> attributes.withLineBreak(lineBreak))));
    }

    private static Score scoreWithMeasures(int count) {
        Track guitar = Track.standardGuitar("Guitarra");
        return new Score("", 120, List.of(new Track("Guitarra", guitar.tuning(), guitar.channel(), measuresOf(count))));
    }

    private static Score twoTrackScoreWithMeasures(int count) {
        Track guitar = Track.standardGuitar("Guitarra");
        Track bass = Track.standardBass("Bajo");
        return new Score("", 120, List.of(
                new Track("Guitarra", guitar.tuning(), guitar.channel(), measuresOf(count)),
                new Track("Bajo", bass.tuning(), bass.channel(), measuresOf(count))));
    }

    private static List<Measure> measuresOf(int count) {
        List<Measure> measures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            measures.add(quarters(4));
        }
        return measures;
    }

    private static Track trackWith(Measure... measures) {
        Track guitar = Track.standardGuitar("Guitarra");
        return new Track("Guitarra", guitar.tuning(), guitar.channel(), List.of(measures));
    }

    private static Measure quarters(int count) {
        return repeated(Duration.quarter(), count);
    }

    private static Measure eighths(int count) {
        return repeated(new Duration(NoteValue.EIGHTH, false), count);
    }

    private static Measure repeated(Duration duration, int count) {
        List<Beat> beats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            beats.add(Beat.of(duration, new Note(1, i % 5)));
        }
        return new Measure(TimeSignature.fourFour(), beats);
    }
}
