package com.gstncaruso.tabpro.format.exchange.musicxml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TupletRunsTest {

    @Test
    void marksTheFirstAndLastBeatOfATripletAsStartAndStop() {
        Duration eighthTriplet = new Duration(NoteValue.EIGHTH, false, Tuplet.of(3));
        Beat one = Beat.of(eighthTriplet, new Note(1, 0));
        Beat two = Beat.of(eighthTriplet, new Note(1, 1));
        Beat three = Beat.of(eighthTriplet, new Note(1, 2));
        List<Beat> beats = List.of(one, two, three);

        Map<Integer, TupletRuns.Mark> marks = TupletRuns.of(beats);

        assertTrue(marks.get(0).start());
        assertTrue(!marks.get(0).stop());
        assertTrue(!marks.get(1).start() && !marks.get(1).stop());
        assertTrue(marks.get(2).stop());
        assertEquals(3, marks.size());
    }

    @Test
    void plainBeatsGetNoMark() {
        Beat plain = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 0));

        Map<Integer, TupletRuns.Mark> marks = TupletRuns.of(List.of(plain));

        assertTrue(marks.isEmpty());
    }

    @Test
    void twoSeparateTripletsAreTwoRuns() {
        Duration eighthTriplet = new Duration(NoteValue.EIGHTH, false, Tuplet.of(3));
        Beat one = Beat.of(eighthTriplet, new Note(1, 0));
        Beat two = Beat.of(eighthTriplet, new Note(1, 1));
        Beat plain = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 2));
        Beat three = Beat.of(eighthTriplet, new Note(1, 3));
        Beat four = Beat.of(eighthTriplet, new Note(1, 4));
        List<Beat> beats = List.of(one, two, plain, three, four);

        Map<Integer, TupletRuns.Mark> marks = TupletRuns.of(beats);

        assertTrue(marks.get(0).start() && marks.get(1).stop());
        assertTrue(marks.get(3).start() && marks.get(4).stop());
        assertEquals(4, marks.size());
    }
}
