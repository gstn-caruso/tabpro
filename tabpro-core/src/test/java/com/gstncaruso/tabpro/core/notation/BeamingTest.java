package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import java.util.List;
import org.junit.jupiter.api.Test;

class BeamingTest {

    private static final Duration EIGHTH = new Duration(NoteValue.EIGHTH, false);
    private static final Duration SIXTEENTH = new Duration(NoteValue.SIXTEENTH, false);
    private static final Duration HALF_DOTTED = new Duration(NoteValue.HALF, true);
    private static final Duration QUARTER = Duration.quarter();

    private static Beat note(Duration duration) {
        return Beat.of(duration, new Note(1, 0));
    }

    private static Beat rest(Duration duration) {
        return Beat.rest(duration);
    }

    @Test
    void beamCountIsZeroForQuarterHalfAndWhole() {
        assertEquals(0, Beaming.beamCount(NoteValue.QUARTER));
        assertEquals(0, Beaming.beamCount(NoteValue.HALF));
        assertEquals(0, Beaming.beamCount(NoteValue.WHOLE));
    }

    @Test
    void beamCountGrowsWithShorterFigures() {
        assertEquals(1, Beaming.beamCount(NoteValue.EIGHTH));
        assertEquals(2, Beaming.beamCount(NoteValue.SIXTEENTH));
        assertEquals(3, Beaming.beamCount(NoteValue.THIRTY_SECOND));
        assertEquals(4, Beaming.beamCount(NoteValue.SIXTY_FOURTH));
    }

    @Test
    void fourQuarterNotesFormNoGroups() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(QUARTER), note(QUARTER), note(QUARTER), note(QUARTER)));
        assertTrue(Beaming.groupsOf(measure).isEmpty());
    }

    @Test
    void eightEighthNotesFormFourGroupsOfTwoOnePerBeat() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH), note(EIGHTH), note(EIGHTH), note(EIGHTH),
                note(EIGHTH), note(EIGHTH), note(EIGHTH), note(EIGHTH)));
        assertEquals(
                List.of(new BeamGroup(0, 1), new BeamGroup(2, 3), new BeamGroup(4, 5), new BeamGroup(6, 7)),
                Beaming.groupsOf(measure));
    }

    @Test
    void fourSixteenthsFillingTheFirstBeatFormOneGroup() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(SIXTEENTH), note(SIXTEENTH), note(SIXTEENTH), note(SIXTEENTH), note(HALF_DOTTED)));
        assertEquals(List.of(new BeamGroup(0, 3)), Beaming.groupsOf(measure));
    }

    @Test
    void aRestInTheMiddleCutsTheGroupInTwo() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH), rest(EIGHTH), note(EIGHTH), note(EIGHTH), rest(new Duration(NoteValue.HALF, false))));
        assertEquals(
                List.of(new BeamGroup(0, 0), new BeamGroup(2, 3)),
                Beaming.groupsOf(measure));
    }

    @Test
    void anEmptyMeasureWithOnlyARestFormsNoGroups() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), QUARTER);
        assertTrue(Beaming.groupsOf(measure).isEmpty());
    }
}
