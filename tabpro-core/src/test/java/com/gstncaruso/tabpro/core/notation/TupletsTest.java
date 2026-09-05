package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Tuplet;
import java.util.List;
import org.junit.jupiter.api.Test;

class TupletsTest {

    private static final Duration EIGHTH = new Duration(NoteValue.EIGHTH, false);
    private static final Duration EIGHTH_TRIPLET = EIGHTH.in(Tuplet.of(3));

    private static Beat note(Duration duration) {
        return Beat.of(duration, new Note(1, 0));
    }

    private static Beat rest(Duration duration) {
        return Beat.rest(duration);
    }

    @Test
    void anEmptyMeasureHasNoGroups() {
        assertTrue(Tuplets.groupsOf(Measure.empty(TimeSignature.fourFour(), Duration.quarter())).isEmpty());
    }

    @Test
    void plainDurationsFormNoGroups() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(note(EIGHTH), note(EIGHTH)));
        assertTrue(Tuplets.groupsOf(measure).isEmpty());
    }

    @Test
    void threeConsecutiveTripletsFormOneGroup() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH_TRIPLET), note(EIGHTH_TRIPLET), note(EIGHTH_TRIPLET), note(Duration.quarter())));

        assertEquals(List.of(new TupletGroup(0, 2, Tuplet.of(3))), Tuplets.groupsOf(measure));
    }

    @Test
    void aRestInsideTheTripletStaysInTheSameGroup() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH_TRIPLET), rest(EIGHTH_TRIPLET), note(EIGHTH_TRIPLET)));

        assertEquals(List.of(new TupletGroup(0, 2, Tuplet.of(3))), Tuplets.groupsOf(measure));
    }

    @Test
    void twoDifferentTupletsBackToBackFormTwoGroups() {
        Duration sixteenthQuintuplet = new Duration(NoteValue.SIXTEENTH, false).in(Tuplet.of(5));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH_TRIPLET), note(EIGHTH_TRIPLET), note(sixteenthQuintuplet)));

        assertEquals(
                List.of(new TupletGroup(0, 1, Tuplet.of(3)), new TupletGroup(2, 2, Tuplet.of(5))),
                Tuplets.groupsOf(measure));
    }

    @Test
    void aSingleTripletNoteIsItsOwnGroup() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(Duration.quarter()), note(EIGHTH_TRIPLET), note(Duration.quarter())));

        List<TupletGroup> groups = Tuplets.groupsOf(measure);
        assertEquals(1, groups.size());
        assertTrue(groups.get(0).isSingle());
    }
}
