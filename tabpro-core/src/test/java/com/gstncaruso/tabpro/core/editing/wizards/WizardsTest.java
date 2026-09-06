package com.gstncaruso.tabpro.core.editing.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WizardsTest {

    @Test
    void transposingRaisesEveryFretOfTheTrack() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(1, 5), new Note(2, 3)));

        Score raised = Transposition.transposeTrack(score, 0, 2);

        assertEquals(7, raised.track(0).measure(0).beat(0).noteOn(1).orElseThrow().fret());
        assertEquals(5, raised.track(0).measure(0).beat(0).noteOn(2).orElseThrow().fret());
    }

    @Test
    void transposingKeepsThePitchWhenTheStringRunsOut() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(1, 0)));

        Score lowered = Transposition.transposeTrack(score, 0, -2);

        Note moved = lowered.track(0).measure(0).beat(0).notes().getFirst();
        Tuning tuning = lowered.track(0).tuning();
        assertEquals(62, tuning.pitchOf(moved).midiNumber());
        assertEquals(2, moved.string());
    }

    @Test
    void transposingLeavesThePercussionAlone() {
        Score score = new Score("Prueba", 120, List.of(Track.percussion("Bateria")));

        assertEquals(score, Transposition.transposeEveryTrack(score, 3));
    }

    /**
     * El manual (Check Bar Duration) dice que el asistente detecta los compases que no
     * suman lo que su medida pide; uno que ya cierra no tiene por que aparecer.
     */
    @Test
    void theDurationCheckFindsTheBarsThatDoNotCloseTheirTime() {
        Score score = scoreWith(Beat.rest(Duration.quarter()));

        List<BarDurationCheck.Finding> findings = BarDurationCheck.run(score);

        assertEquals(1, findings.size());
        assertTrue(findings.getFirst().tooShort());
    }

    @Test
    void theDurationCheckIgnoresABarThatAlreadyClosesItsTime() {
        Score score = scoreWith(
                Beat.of(Duration.quarter(), new Note(1, 0)), Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)), Beat.of(Duration.quarter(), new Note(1, 3)));

        List<BarDurationCheck.Finding> findings = BarDurationCheck.run(score);

        assertTrue(findings.isEmpty());
    }

    @Test
    void theDurationCheckFlagsABarThatWentPastItsTime() {
        Score score = scoreWith(
                Beat.of(Duration.of(NoteValue.WHOLE), new Note(1, 0)), Beat.rest(Duration.quarter()));

        List<BarDurationCheck.Finding> findings = BarDurationCheck.run(score);

        assertEquals(1, findings.size());
        assertTrue(findings.getFirst().tooLong());
        assertFalse(findings.getFirst().tooShort());
    }

    @Test
    void theDurationCheckPointsAtTheExactBarThatFailed() {
        Measure completeBar = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)), Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)), Beat.of(Duration.quarter(), new Note(1, 3))));
        Measure shortBar = new Measure(TimeSignature.fourFour(),
                List.of(Beat.of(Duration.quarter(), new Note(1, 4))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25),
                List.of(completeBar, shortBar));
        Score score = new Score("Prueba", 120, List.of(track));

        List<BarDurationCheck.Finding> findings = BarDurationCheck.run(score);

        assertEquals(1, findings.size());
        assertEquals(0, findings.getFirst().trackIndex());
        assertEquals(1, findings.getFirst().measureIndex());
    }

    /**
     * El manual (Complete/Reduce Bars with Rests) dice que completa con silencios los
     * compases cortos: no alcanza con que cierre, tienen que ser los silencios correctos
     * -los mas largos que entren primero- en el lugar correcto -despues de lo que ya sonaba-.
     */
    @Test
    void theRestFillerCompletesAShortBar() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(1, 5)));

        Score filled = RestFiller.run(score, MeasureRange.wholeScore(1));

        assertTrue(filled.track(0).measure(0).isComplete());
    }

    @Test
    void theRestFillerFillsTheGapWithTheLargestRestsFirst() {
        Beat note = Beat.of(Duration.quarter(), new Note(1, 5));
        Score score = scoreWith(note);

        Score filled = RestFiller.run(score, MeasureRange.wholeScore(1));

        assertEquals(
                List.of(note, Beat.rest(Duration.of(NoteValue.HALF)), Beat.rest(Duration.of(NoteValue.QUARTER))),
                filled.track(0).measure(0).beats());
    }

    /** El manual dice explicitamente "(or empty bars)": un compas sin nada tambien se completa. */
    @Test
    void theRestFillerCompletesAnEmptyBar() {
        Measure empty = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(empty));
        Score score = new Score("Prueba", 120, List.of(track));

        Score filled = RestFiller.run(score, MeasureRange.wholeScore(1));

        assertTrue(filled.track(0).measure(0).isComplete());
        assertTrue(filled.track(0).measure(0).beats().stream().allMatch(Beat::isRest));
    }

    @Test
    void theRestFillerLeavesACompleteBarAlone() {
        Score score = scoreWith(
                Beat.of(Duration.quarter(), new Note(1, 0)), Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)), Beat.of(Duration.quarter(), new Note(1, 3)));

        Score result = RestFiller.run(score, MeasureRange.wholeScore(1));

        assertEquals(score, result);
    }

    @Test
    void theRestFillerTakesTheSpareRestsOffALongBar() {
        Score score = scoreWith(
                Beat.of(Duration.of(NoteValue.WHOLE), new Note(1, 5)), Beat.rest(Duration.quarter()));

        Score reduced = RestFiller.run(score, MeasureRange.wholeScore(1));

        assertTrue(reduced.track(0).measure(0).isComplete());
        assertEquals(1, reduced.track(0).measure(0).beats().size());
    }

    @Test
    void theRestFillerRemovesAsManySpareRestsAsItTakesToClose() {
        Score score = scoreWith(
                Beat.of(Duration.quarter(), new Note(1, 5)),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()));

        Score reduced = RestFiller.run(score, MeasureRange.wholeScore(1));

        assertTrue(reduced.track(0).measure(0).isComplete());
        assertEquals(4, reduced.track(0).measure(0).beats().size());
    }

    /**
     * El manual solo promete borrar silencios de mas; un compas largo por notas -no por
     * silencios- no tiene de donde sacar, asi que queda largo.
     */
    @Test
    void theRestFillerCannotShrinkABarThatIsTooLongOnlyWithNotes() {
        Score score = scoreWith(
                Beat.of(Duration.of(NoteValue.WHOLE), new Note(1, 0)), Beat.of(Duration.quarter(), new Note(1, 1)));

        Score result = RestFiller.run(score, MeasureRange.wholeScore(1));

        assertTrue(result.track(0).measure(0).isTooLong());
        assertEquals(2, result.track(0).measure(0).beats().size());
    }

    /**
     * El manual (Bar Arranger) dice que reacomoda los compases para que su posicion sea
     * musicalmente correcta; el tip de captura MIDI (linea 846 del manual) describe justo
     * este caso: cambiar el ritmo al final corre los compases de lugar y hay que recomponerlos.
     */
    @Test
    void theBarArrangerPushesTheSpareBeatsToTheNextBar() {
        Measure crowded = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)),
                Beat.of(Duration.quarter(), new Note(1, 4)),
                Beat.of(Duration.quarter(), new Note(1, 5))));
        Score score = new Score("Prueba", 120,
                List.of(new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(crowded))));

        Score arranged = BarArranger.run(score);

        assertEquals(2, arranged.track(0).measureCount());
        assertEquals(4, arranged.track(0).measure(0).beats().size());
        assertEquals(5, arranged.track(0).measure(1).beat(0).noteOn(1).orElseThrow().fret());
    }

    @Test
    void theBarArrangerBorrowsFromTheNextBarWhenOneIsTooShort() {
        Measure crowded = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 1)), Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)), Beat.of(Duration.quarter(), new Note(1, 4)),
                Beat.of(Duration.quarter(), new Note(1, 5))));
        Measure sparse = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 6)), Beat.of(Duration.quarter(), new Note(1, 7)),
                Beat.of(Duration.quarter(), new Note(1, 8))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(crowded, sparse));
        Score score = new Score("Prueba", 120, List.of(track));

        Score arranged = BarArranger.run(score);

        assertEquals(2, arranged.track(0).measureCount());
        assertTrue(arranged.track(0).measure(0).isComplete());
        assertTrue(arranged.track(0).measure(1).isComplete());
        assertEquals(5, arranged.track(0).measure(1).beat(0).noteOn(1).orElseThrow().fret());
    }

    @Test
    void theBarArrangerLeavesAlreadyCorrectBarsUntouched() {
        Score score = scoreWith(
                Beat.of(Duration.quarter(), new Note(1, 0)), Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)), Beat.of(Duration.quarter(), new Note(1, 3)));

        Score arranged = BarArranger.run(score);

        assertEquals(score, arranged);
    }

    @Test
    void theStringOptionsWizardOnlyTouchesTheStringsItIsGiven() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(1, 5), new Note(6, 0)));

        Score muted = StringOptions.applyOrnament(
                score, 0, MeasureRange.wholeScore(1), Set.of(6), Ornament.PALM_MUTE, true);

        assertFalse(muted.track(0).measure(0).beat(0).noteOn(1).orElseThrow().has(Ornament.PALM_MUTE));
        assertTrue(muted.track(0).measure(0).beat(0).noteOn(6).orElseThrow().has(Ornament.PALM_MUTE));
    }

    @Test
    void theDynamicWizardWritesTheDynamicOfTheStringsItIsGiven() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(1, 5)));

        Score louder = StringOptions.applyDynamic(
                score, 0, MeasureRange.wholeScore(1), Set.of(1), Dynamic.FORTISSIMO);

        assertEquals(Dynamic.FORTISSIMO,
                louder.track(0).measure(0).beat(0).noteOn(1).orElseThrow().effects().dynamic());
    }

    @Test
    void theAutomaticFingeringKeepsEveryPitch() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(6, 12)));
        Tuning tuning = score.track(0).tuning();
        int before = tuning.pitchOf(score.track(0).measure(0).beat(0).notes().getFirst()).midiNumber();

        Score fingered = AutomaticFingering.run(score, 0);

        Note after = fingered.track(0).measure(0).beat(0).notes().getFirst();
        assertEquals(before, tuning.pitchOf(after).midiNumber());
    }

    private static Score scoreWith(Beat... beats) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beats));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Prueba", 120, List.of(track));
    }
}
