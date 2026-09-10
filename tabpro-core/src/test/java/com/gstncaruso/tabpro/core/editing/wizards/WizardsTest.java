package com.gstncaruso.tabpro.core.editing.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    @Test
    void theRestFillerCannotShrinkABarThatIsTooLongOnlyWithNotes() {
        Score score = scoreWith(
                Beat.of(Duration.of(NoteValue.WHOLE), new Note(1, 0)), Beat.of(Duration.quarter(), new Note(1, 1)));

        Score result = RestFiller.run(score, MeasureRange.wholeScore(1));

        assertTrue(result.track(0).measure(0).isTooLong());
        assertEquals(2, result.track(0).measure(0).beats().size());
    }

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

    @Test
    void theAutomaticFingeringSeparatesAChordOntoDifferentStrings() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(1, 0), new Note(1, 5)));
        Tuning tuning = score.track(0).tuning();
        List<Note> before = score.track(0).measure(0).beat(0).notes();
        int firstPitch = tuning.pitchOf(before.get(0)).midiNumber();
        int secondPitch = tuning.pitchOf(before.get(1)).midiNumber();

        Score fingered = AutomaticFingering.run(score, 0);

        List<Note> after = fingered.track(0).measure(0).beat(0).notes();
        assertEquals(2, after.size());
        assertNotEquals(after.get(0).string(), after.get(1).string());
        assertEquals(Set.of(firstPitch, secondPitch),
                Set.of(tuning.pitchOf(after.get(0)).midiNumber(), tuning.pitchOf(after.get(1)).midiNumber()));
    }

    @Test
    void theAutomaticFingeringFollowsTheHandInsteadOfJumpingToTheLowestFret() {
        Score score = scoreWith(
                Beat.of(Duration.quarter(), new Note(1, 12)), Beat.of(Duration.quarter(), new Note(1, 7)));
        Tuning tuning = score.track(0).tuning();

        Score fingered = AutomaticFingering.run(score, 0);

        Note second = fingered.track(0).measure(0).beat(1).notes().getFirst();
        assertEquals(71, tuning.pitchOf(second).midiNumber());
        assertEquals(2, second.string());
        assertEquals(12, second.fret());
    }

    @Test
    void theAutomaticFingeringLeavesANoteAloneWhenNoStringCanReachIt() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(1, 50)));

        Score fingered = AutomaticFingering.run(score, 0);

        Note after = fingered.track(0).measure(0).beat(0).notes().getFirst();
        assertEquals(1, after.string());
        assertEquals(50, after.fret());
    }

    private static Score scoreWith(Beat... beats) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beats));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Prueba", 120, List.of(track));
    }
}
